# Skill — Integrar AppLogger en una App Android (Kotlin)

**SDK versión:** 0.1.0-alpha.1  
**Plataformas:** Android Mobile (API 23+) · Android TV (API 23+) · Wear OS  
**Lenguaje:** Kotlin  
**Distribución:** JitPack · GitHub Packages

---

## Prerrequisitos

| Requisito | Verificar con | Valor mínimo |
|---|---|---|
| Android Gradle Plugin | `build.gradle.kts` raíz | `8.0+` |
| `compileSdk` | módulo `app/build.gradle.kts` | `33+` |
| `minSdk` | módulo `app/build.gradle.kts` | `23` |
| Kotlin | `build.gradle.kts` raíz | `1.9+` |
| Coroutines | dependencias del proyecto | `1.7+` |
| Acceso a internet | `AndroidManifest.xml` | `INTERNET` + `ACCESS_NETWORK_STATE` |

---

## Paso 1 — Añadir repositorio JitPack

En `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

## Paso 2 — Añadir dependencias

En `app/build.gradle.kts`:

```kotlin
dependencies {
    // Core (obligatorio)
    implementation("com.github.devzucca.appLoggers:logger-core:v0.1.0-alpha.1")

    // Transporte Supabase (obligatorio si usas Supabase como backend)
    implementation("com.github.devzucca.appLoggers:logger-transport-supabase:v0.1.0-alpha.1")

    // Utilidades de testing (solo en tests)
    testImplementation("com.github.devzucca.appLoggers:logger-test:v0.1.0-alpha.1")
    androidTestImplementation("com.github.devzucca.appLoggers:logger-test:v0.1.0-alpha.1")
}
```

---

## Paso 3 — Añadir permisos

En `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

El SDK **no requiere** ningún otro permiso. No accede a localización, contactos ni almacenamiento externo.

---

## Paso 4 — Configurar credenciales locales

En `local.properties` (nunca commitear):

```properties
# Supabase (obtener de: supabase.com → Settings → API)
appLogger.url=https://TU-PROYECTO.supabase.co
appLogger.anonKey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Comportamiento
appLogger.debug=true          # true = Logcat, false = backend remoto
appLogger.logToConsole=true
appLogger.batchSize=20
appLogger.flushIntervalSeconds=30
appLogger.maxStackTraceLines=50

# Opciones avanzadas de buffer (nuevas en 0.2.0)
appLogger.bufferSizeStrategy=FIXED           # FIXED, ADAPTIVE_TO_RAM, ADAPTIVE_TO_LOG_RATE
appLogger.bufferOverflowPolicy=DISCARD_OLDEST # DISCARD_OLDEST, DISCARD_NEWEST, PRIORITY_AWARE
appLogger.offlinePersistenceMode=NONE        # NONE, CRITICAL_ONLY, ALL
```

Mapear a `BuildConfig` en `app/build.gradle.kts`:

```kotlin
import java.util.Properties

android {
    buildFeatures { buildConfig = true }

    defaultConfig {
        val props = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) load(f.inputStream())
        }
        buildConfigField("String",  "LOGGER_URL",   "\"${props["appLogger.url"] ?: ""}\"")
        buildConfigField("String",  "LOGGER_KEY",   "\"${props["appLogger.anonKey"] ?: ""}\"")
        buildConfigField("Boolean", "LOGGER_DEBUG", "${props["appLogger.debug"] ?: false}")
        buildConfigField("String",  "LOGGER_BUFFER_STRATEGY", "\"${props["appLogger.bufferSizeStrategy"] ?: "FIXED"}\"")
        buildConfigField("String",  "LOGGER_OVERFLOW_POLICY", "\"${props["appLogger.bufferOverflowPolicy"] ?: "DISCARD_OLDEST"}\"")
        buildConfigField("String",  "LOGGER_PERSISTENCE_MODE", "\"${props["appLogger.offlinePersistenceMode"] ?: "NONE"}\"")
    }
}
```

---

## Paso 5 — Inicializar en Application

Crear o actualizar la clase `Application`:

```kotlin
import android.app.Application
import com.applogger.core.AppLoggerConfig
import com.applogger.core.AppLoggerSDK
import com.applogger.transport.supabase.SupabaseTransport

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val transport = SupabaseTransport(
            endpoint = BuildConfig.LOGGER_URL,
            apiKey   = BuildConfig.LOGGER_KEY
        )

        AppLoggerSDK.initialize(
            context   = this,
            config    = AppLoggerConfig.Builder()
                .endpoint(BuildConfig.LOGGER_URL)
                .apiKey(BuildConfig.LOGGER_KEY)
                .debugMode(BuildConfig.LOGGER_DEBUG)
                .batchSize(20)
                .flushIntervalSeconds(30)
                .build(),
            transport = transport
        )
    }
}
```

Declarar en `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApp"
    ... >
```

> **Idempotente**: llamadas posteriores a `initialize()` son ignoradas silenciosamente. Seguro en multi-process.

---

## Paso 6 — Usar el logger

```kotlin
import com.applogger.core.AppLoggerSDK

// --- Niveles disponibles ---

// DEBUG — solo visible en modo debug (isDebugMode = true)
AppLoggerSDK.debug("TAG", "Valor de variable: $value")
AppLoggerSDK.debug("TAG", "Con datos extra", extra = mapOf("key" to "value"))

// INFO — flujos normales productivos
AppLoggerSDK.info("PLAYER", "Playback started")
AppLoggerSDK.info("AUTH",   "Login successful", extra = mapOf("method" to "google"))

// WARN — comportamiento inesperado pero recuperable
AppLoggerSDK.warn("NETWORK", "Slow response", anomalyType = "HIGH_LATENCY",
    extra = mapOf("latency_ms" to "1200"))

// ERROR — fallo que el usuario percibe
AppLoggerSDK.error("PAYMENT", "Transaction failed", throwable = exception)
AppLoggerSDK.error("API",     "Parse error",        throwable = e,
    extra = mapOf("endpoint" to "/v2/orders"))

// CRITICAL — fallo que bloquea la app
AppLoggerSDK.critical("AUTH", "Token refresh failed", throwable = exception)

// METRIC — datos cuantitativos de performance
AppLoggerSDK.metric("screen_load_time", 1234.0, "ms",
    tags = mapOf("screen" to "HomeScreen", "cold_start" to "true"))

// Flush manual (opcional — ocurre automáticamente en background)
AppLoggerSDK.flush()
```

---

## Paso 7 — Buenas prácticas de contenido

```kotlin
// ✅ Loguear contexto técnico
AppLoggerSDK.error("STREAM", "HLS segment fetch failed",
    extra = mapOf("segment" to "42", "cdn" to "us-east-1"))

// ✅ Tags consistentes por módulo/feature
object LogTags {
    const val PLAYER  = "PLAYER"
    const val NETWORK = "NETWORK"
    const val AUTH    = "AUTH"
    const val PAYMENT = "PAYMENT"
}

// ❌ NUNCA loguear datos del usuario
AppLoggerSDK.error("AUTH", "Error para: ${user.email}")  // PII — prohibido

// ❌ NUNCA loguear tokens o credenciales
AppLoggerSDK.debug("AUTH", "Token: $accessToken")        // Secreto — prohibido
```

---

## Paso 8 — Configuración para Android TV

El SDK **detecta automáticamente** Android TV y aplica valores conservadores. No se requiere código adicional:

| Comportamiento en TV | Valor automático |
|---|---|
| Batch size | 5 (vs 20 Mobile) |
| Flush interval | 60 s (vs 30 s) |
| Max stack trace lines | 5 (vs 50) |
| Flush only when idle | `true` |
| Rate limit por tag/min | 30 (vs 120) |
| Buffer en memoria | 100 eventos (vs 1000) |

Se puede sobreescribir cualquier valor en `AppLoggerConfig.Builder()` si el default no es adecuado.

---

## Paso 9 — User ID anónimo con consentimiento

```kotlin
// Solo tras aceptación explícita de la política de privacidad
fun onUserConsentGranted() {
    val anonymousId = getOrCreateAnonymousId()  // UUID generado en el dispositivo
    AppLoggerSDK.setAnonymousUserId(anonymousId)
}

// Para revocar (derecho al olvido)
fun onUserConsentRevoked() {
    AppLoggerSDK.clearAnonymousUserId()
}

private fun getOrCreateAnonymousId(): String {
    val prefs = getSharedPreferences("app_logger_prefs", Context.MODE_PRIVATE)
    return prefs.getString("anon_user_id", null)
        ?: java.util.UUID.randomUUID().toString().also { id ->
            prefs.edit().putString("anon_user_id", id).apply()
        }
}
```

---

## Paso 10 — Testing sin red

```kotlin
import com.applogger.test.FakeTransport
import com.applogger.test.InMemoryLogger

// Opción A: verificar eventos enviados al transporte
@Test
fun `payment failure is logged`() = runTest {
    val transport = FakeTransport(shouldSucceed = true)
    // inyectar transport al componente bajo test...
    assertEquals(1, transport.sentEvents.size)
    assertEquals("ERROR", transport.sentEvents.first().level.name)
}

// Opción B: verificar el logger directamente
@Test
fun `component logs correct level`() {
    val logger = InMemoryLogger()
    myComponent.setLogger(logger)
    myComponent.doSomething()
    logger.assertLogged(LogLevel.ERROR, tag = "PAYMENT")
    logger.assertNotLogged(LogLevel.DEBUG)
}

// Opción C: no-op logger para tests donde el logger no es el foco
val noOp = com.applogger.core.internal.NoOpLogger()
```

---

## Errores comunes

| Error | Causa | Solución |
|---|---|---|
| `AppLogger: production endpoint must use HTTPS` | Endpoint HTTP con `debugMode = false` | Usar `https://` o activar `debugMode = true` en desarrollo |
| No llegan eventos a Supabase | `isDebugMode = true` con `consoleOutput = false` | Eventos van a Logcat — cambiar a `isDebugMode = false` en staging/prod |
| `ClassNotFoundException: SupabaseTransport` | Falta dependencia `logger-transport-supabase` | Añadir al `build.gradle.kts` del módulo |
| Build del SDK falla en Windows (`verifyMigrations`) | SQLite DLL lock en Windows | El `build.gradle.kts` del SDK ya incluye `isWindowsHost` guard — no afecta a la app consumidora |
| Eventos duplicados en Supabase | `initialize()` llamado más de una vez | `initialize()` es idempotente — la segunda llamada es ignorada |

---

## Referencia rápida de la API pública

```kotlin
// Inicialización (una vez, en Application.onCreate)
AppLoggerSDK.initialize(context, config, transport)

// Logging
AppLoggerSDK.debug(tag, message, extra?)
AppLoggerSDK.info(tag, message, extra?)
AppLoggerSDK.warn(tag, message, anomalyType?, extra?)
AppLoggerSDK.error(tag, message, throwable?, extra?)
AppLoggerSDK.critical(tag, message, throwable?, extra?)
AppLoggerSDK.metric(name, value, unit, tags?)

// Control
AppLoggerSDK.flush()
AppLoggerSDK.setAnonymousUserId(userId)
AppLoggerSDK.clearAnonymousUserId()

// Health check
val health = AppLoggerHealth.snapshot()
health.isInitialized       // Boolean
health.transportAvailable  // Boolean
health.bufferedEvents      // Int
```
