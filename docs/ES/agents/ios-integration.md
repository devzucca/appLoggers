# Skill — Integrar AppLogger en iOS (KMP build + consumo host)

**SDK versión:** 0.1.0-alpha.1  
**Plataforma:** iOS 14+  
**Lenguaje del SDK:** Kotlin Multiplatform  
**Lenguaje host (si aplica):** Swift 5.9+  
**Distribución host:** Swift Package Manager · CocoaPods

> El SDK compila el módulo `iosMain` de Kotlin Multiplatform como **XCFramework** (`AppLogger.xcframework`).  
> El entry point iOS es `AppLoggerIos.shared` — **distinto** del singleton Android `AppLoggerSDK`.

> Alcance de este documento: build iOS del SDK KMP y consumo desde app host cuando exista.  
> Si tu app es KMP end-to-end, no necesitas lógica del SDK en Swift; solo enlazar el framework iOS generado por Gradle.

---

## Prerrequisitos

| Requisito | Valor mínimo |
|---|---|
| Xcode | 15.0+ |
| iOS Deployment Target | iOS 14.0 |
| Swift | 5.9+ |
| Acceso a red (Info.plist) | `NSAppTransportSecurity` → HTTPS (obligatorio en producción) |

---

## Flujo recomendado para esta auditoría (KMP)

1. Implementar y mantener el SDK en Kotlin (`commonMain` + `iosMain`).
2. Generar framework iOS desde Gradle (KMP).
3. Validar el artefacto iOS generado (framework/XCFramework).
4. Solo si hay app host nativa: consumir por SPM o CocoaPods desde Swift.

---

## Opción A — Swift Package Manager (recomendado)

> Esta opción aplica cuando el consumidor del SDK es una app iOS nativa Swift.

### A.1 Añadir el paquete al proyecto

En Xcode → File → Add Package Dependencies:

```
https://github.com/devzucca/appLoggers
```

O en `Package.swift` de tu proyecto:

```swift
dependencies: [
    .package(
        url: "https://github.com/devzucca/appLoggers",
        from: "0.1.0-alpha.1"
    )
],
targets: [
    .target(
        name: "MyApp",
        dependencies: [
            .product(name: "AppLogger", package: "appLoggers")
        ]
    )
]
```

### A.2 Targets disponibles en el SPM

El `Package.swift` del SDK declara:

```swift
// sdk/Package.swift
.iOS(.v14), .macOS(.v12), .tvOS(.v14), .watchOS(.v7)
```

---

## Opción B — CocoaPods

> Esta opción aplica cuando el consumidor del SDK es una app iOS nativa Swift.

En `Podfile`:

```ruby
pod 'AppLogger', :git => 'https://github.com/devzucca/appLoggers.git', :tag => 'v0.1.0-alpha.1'
```

El `AppLogger.podspec` en el SDK especifica:
- `s.ios.deployment_target = '14.0'`
- `s.tvos.deployment_target = '14.0'`

---

## Si tu proyecto es KMP (sin app Swift nativa)

En un flujo Kotlin Multiplatform, el módulo shared compila `iosMain` a framework nativo iOS.

- Se desarrolla el SDK en Kotlin.
- Se compila para iOS con Gradle (framework/XCFramework).
- No necesitas mantener `Package.swift` en tu app si no distribuyes ni consumes por SPM.
- Swift solo es necesario en la capa host iOS cuando existe una app iOS nativa consumidora.

---

## Paso 1 — Inicializar en App entry point

```swift
import AppLogger

@main
struct MyApp: App {

    init() {
        setupLogger()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func setupLogger() {
        let transport = SupabaseTransport(
            endpoint: "https://TU-PROYECTO.supabase.co",
            apiKey: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )

        AppLoggerIos.shared.initialize(
            config: AppLoggerConfig.Builder()
                .endpoint(url: "https://TU-PROYECTO.supabase.co")
                .apiKey(key: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .debugMode(debug: false)         // true = consola, false = backend
                .batchSize(size: 20)
                .flushIntervalSeconds(sec: 30)
                .maxStackTraceLines(lines: 50)
                // Opciones avanzadas (disponibles en Kotlin; en iOS requieren extensión nativa):
                // .bufferSizeStrategy(strategy: .FIXED) // o .ADAPTIVE_TO_RAM, .ADAPTIVE_TO_LOG_RATE
                // .bufferOverflowPolicy(policy: .DISCARD_OLDEST) // o .DISCARD_NEWEST, .PRIORITY_AWARE
                // .offlinePersistenceMode(mode: .NONE) // o .CRITICAL_ONLY, .ALL
                .build(),
            transport: transport
        )
    }
}
```

> **Credenciales en producción**: nunca hardcodear keys en el código fuente.  
> Usar Variables de entorno de Xcode (`$(LOGGER_URL)`) o un archivo `Config.xcconfig` excluido del repositorio.

---

## Paso 2 — Gestionar credenciales

### Config.xcconfig (excluir en .gitignore)

```text
// AppLoggerConfig.xcconfig — NO commitear
LOGGER_URL = https://TU-PROYECTO.supabase.co
LOGGER_KEY = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
LOGGER_DEBUG = NO
```

### Info.plist — leer en código

```swift
private func loggerURL() -> String {
    Bundle.main.object(forInfoDictionaryKey: "LOGGER_URL") as? String ?? ""
}

private func loggerKey() -> String {
    Bundle.main.object(forInfoDictionaryKey: "LOGGER_KEY") as? String ?? ""
}
```

---

## Paso 3 — Usar el logger

```swift
import AppLogger

// DEBUG — visible solo en modo debug (debugMode: true)
AppLoggerIos.shared.debug(tag: "TAG", message: "Valor: \(value)", extra: nil)

// INFO — flujos normales productivos
AppLoggerIos.shared.info(tag: "PLAYER", message: "Playback started", extra: nil)
AppLoggerIos.shared.info(
    tag: "AUTH",
    message: "Login successful",
    extra: ["method": "apple_sign_in"]
)

// WARN — comportamiento inesperado pero recuperable
AppLoggerIos.shared.warn(
    tag: "NETWORK",
    message: "Slow response",
    anomalyType: "HIGH_LATENCY",
    extra: ["latency_ms": "1400"]
)

// ERROR — fallo que el usuario percibe
AppLoggerIos.shared.error(
    tag: "PAYMENT",
    message: "Transaction failed",
    throwable: nil,       // Kotlin Throwable — pasar nil en iOS
    extra: ["order_id": "ORD-123"]
)

// CRITICAL — fallo que bloquea la app
AppLoggerIos.shared.critical(
    tag: "AUTH",
    message: "Token refresh failed",
    throwable: nil,
    extra: nil
)

// METRIC — datos de performance
AppLoggerIos.shared.metric(
    name: "screen_load_time",
    value: 1234.0,
    unit: "ms",
    tags: ["screen": "HomeView"]
)

// Flush manual (ocurre automáticamente cada flushIntervalSeconds)
AppLoggerIos.shared.flush()
```

---

## Paso 4 — Captura de errores Swift/Objective-C

La clase `IosCrashHandler` instala un handler para excepciones no capturadas (solo en `debugMode = false`). Para errores Swift, envuelve los puntos críticos:

```swift
do {
    try loadUserData()
} catch {
    AppLoggerIos.shared.error(
        tag: "DATA",
        message: "Load user data failed: \(error.localizedDescription)",
        throwable: nil,
        extra: ["error_type": String(describing: type(of: error))]
    )
}
```

> **Nota**: el parámetro `throwable` acepta un `KotlinThrowable` del bridge KMP. En iOS puro, pasar `nil` y usar `extra` para incluir el detalle del error Swift.

---

## Paso 5 — User ID anónimo con consentimiento

```swift
// Solo tras aceptación explícita
func onPrivacyPolicyAccepted() {
    let anonymousId = getOrCreateAnonymousId()
    AppLoggerIos.shared.setAnonymousUserId(userId: anonymousId)
}

// Para revocar
func onPrivacyPolicyRevoked() {
    AppLoggerIos.shared.clearAnonymousUserId()
}

private func getOrCreateAnonymousId() -> String {
    let key = "app_logger_anon_user_id"
    if let stored = UserDefaults.standard.string(forKey: key) {
        return stored
    }
    let newId = UUID().uuidString
    UserDefaults.standard.set(newId, forKey: key)
    return newId
}
```

---

## Paso 6 — Buenas prácticas de contenido

```swift
// ✅ Contexto técnico, sin PII
AppLoggerIos.shared.error(
    tag: "STREAM",
    message: "Segment fetch failed",
    throwable: nil,
    extra: ["segment_index": "42", "cdn": "us-east-1"]
)

// ✅ Tags constantes por módulo
enum LogTags {
    static let player  = "PLAYER"
    static let network = "NETWORK"
    static let auth    = "AUTH"
    static let payment = "PAYMENT"
}

// ❌ NUNCA loguear datos del usuario
AppLoggerIos.shared.error(tag: "AUTH", message: "Error for \(user.email)", ...)

// ❌ NUNCA loguear tokens
AppLoggerIos.shared.debug(tag: "AUTH", message: "Token: \(accessToken)", ...)
```

---

## Paso 7 — Health check en UI (opcional)

```swift
import AppLogger

struct DebugDashboard: View {
    var body: some View {
        VStack {
            Text("Logger: \(AppLoggerHealth.shared.initialized ? "✅" : "❌")")
            Text("Transport: \(AppLoggerHealth.shared.transportAvailable ? "✅" : "offline")")
            Text("Buffered: \(AppLoggerHealth.shared.bufferedEventCount) eventos")
            Text("Overflow: \(AppLoggerHealth.shared.eventsDroppedDueToBufferOverflow) descartados")
            Text("Buffer %: \(String(format: "%.1f", AppLoggerHealth.shared.bufferUtilizationPercentage))%")
        }
    }
}
```

> Mostrar `AppLoggerHealth` solo en builds de debug/staging, no en producción.

---

## Paso 8 — Manejo de desconexión

El SDK maneja automáticamente la pérdida de conectividad:

- **Buffer local**: eventos encolados en memoria mientras no hay red.
- **Reintento**: backoff exponencial con jitter (máx 5 reintentos).
- **Dead Letter Queue**: eventos que no se enviaron tras reintentos agotados.
- **Flush en reconexión**: cuando el transporte recupera conectividad, se envían automáticamente los eventos pendientes.

No se requiere intervención manual. Sin embargo, puedes forzar un flush en momentos clave (ej. al pasar a background):

```swift
// En AppDelegate o SceneDelegate
func sceneDidEnterBackground(_ scene: UIScene) {
    AppLoggerIos.shared.flush()
}
```

---

## Paso 9 — Opciones avanzadas de buffer (Kotlin)

Las siguientes opciones están disponibles en el `AppLoggerConfig.Builder` de Kotlin. En iOS, para usarlas, se debe añadir una extensión nativa en `iosMain` que exponga estos métodos al bridge Swift. Esto está planificado para la versión 0.2.0.

```kotlin
// Solo disponible en Kotlin (por ahora)
AppLoggerConfig.Builder()
    .bufferSizeStrategy(strategy: BufferSizeStrategy) // FIXED, ADAPTIVE_TO_RAM, ADAPTIVE_TO_LOG_RATE
    .bufferOverflowPolicy(policy: BufferOverflowPolicy) // DISCARD_OLDEST, DISCARD_NEWEST, PRIORITY_AWARE
    .offlinePersistenceMode(mode: OfflinePersistenceMode) // NONE, CRITICAL_ONLY, ALL
```

**Estrategias de buffer:**

- `FIXED`: tamaño fijo (default: 1000 móvil, 100 TV/iOS).
- `ADAPTIVE_TO_RAM`: calcula tamaño como % de RAM disponible (0.1%, min 50, max 5000).
- `ADAPTIVE_TO_LOG_RATE`: ajusta dinámicamente según tasa de eventos (futuro).

**Políticas de overflow:**

- `DISCARD_OLDEST`: descarta el evento más antiguo (default).
- `DISCARD_NEWEST`: descarta el evento más reciente.
- `PRIORITY_AWARE`: descarta por prioridad (DEBUG → INFO → WARN → ERROR → CRITICAL), preservando críticos.

**Persistencia offline:**

- `NONE`: solo memoria (default).
- `CRITICAL_ONLY`: guarda en SQLite solo eventos ERROR/CRITICAL (para apps reguladas).
- `ALL`: guarda todos los eventos en SQLite (auditoría completa).

---

## Diferencias entre SDK Android e iOS

| Aspecto | Android | iOS |
|---|---|---|
| Entry point | `AppLoggerSDK` (object/singleton) | `AppLoggerIos.shared` (instance) |
| Config builder | `AppLoggerConfig.Builder()` | `AppLoggerConfig.Builder()` (mismo KMP) |
| Detección de plataforma | `PlatformDetector` (detecta TV) | `PlatformDetector` retorna `JVM` en iOS — sin auto-adapt TV |
| Detección de conexión | WiFi / Cellular / Ethernet (via `ConnectivityManager`) | `"unknown"` (requiere `Reachability` framework adicional) |
| Lifecycle observer | `ProcessLifecycleOwner` (automático) | `flush()` manual en `sceneDidEnterBackground` |
| Crash handler | `AndroidCrashHandler` (UnhandledExceptionHandler) | `IosCrashHandler` (NSSetUncaughtExceptionHandler) |
| `throwable` en logs | `Throwable` real de Kotlin | `null` — el error Swift se pasa en `extra` |

---

## Recomendación: flush en background iOS

A diferencia de Android (que usa `ProcessLifecycleOwner`), en iOS es recomendable hacer flush explícito al pasar a background:

```swift
// En AppDelegate o SceneDelegate
func sceneDidEnterBackground(_ scene: UIScene) {
    AppLoggerIos.shared.flush()
}
```

---

## Errores comunes

| Error | Causa | Solución |
|---|---|---|
| `AppLogger.xcframework` no encontrado | SPM no descargó el paquete | Xcode → File → Packages → Resolve Package Versions |
| `AppLoggerSDK` no existe en iOS | Se usó el nombre Android en Swift | Usar `AppLoggerIos.shared` (no `AppLoggerSDK`) |
| Eventos no llegan a Supabase en debug | `debugMode: true` activo | Cambiar a `debugMode: false` para envío real al backend |
| `NSAppTransportSecurity` error | URL HTTP en lugar de HTTPS | El builder requiere HTTPS en producción — usar `https://` |
| No hay `connectionType` en deviceInfo | `IosDeviceInfoProvider` retorna `"unknown"` | Es un campo metadata; no bloquea el funcionamiento del SDK |

---

## Referencia rápida de la API iOS

```swift
// Inicialización (una vez, en App.init())
AppLoggerIos.shared.initialize(config:, transport:)

// Logging
AppLoggerIos.shared.debug(tag:, message:, extra:)
AppLoggerIos.shared.info(tag:, message:, extra:)
AppLoggerIos.shared.warn(tag:, message:, anomalyType:, extra:)
AppLoggerIos.shared.error(tag:, message:, throwable:, extra:)
AppLoggerIos.shared.critical(tag:, message:, throwable:, extra:)
AppLoggerIos.shared.metric(name:, value:, unit:, tags:)

// Control
AppLoggerIos.shared.flush()
AppLoggerIos.shared.setAnonymousUserId(userId:)
AppLoggerIos.shared.clearAnonymousUserId()
```
