# Skill — Integrar AppLogger en iOS con Kotlin Multiplatform

**SDK versión:** 0.1.0-alpha.1  
**Plataforma:** iOS 14+  
**Lenguaje del SDK y host:** Kotlin Multiplatform  
**Política de proyecto:** no usar integración host nativa externa para nuevas implementaciones

> Esta guía describe un flujo KMP puro: configuración, inicialización y uso del logger desde código Kotlin.
> No requiere gestores de paquetes host externos ni código de host nativo externo.

---

## Objetivo de esta guía

Al terminar, vas a poder:

1. Configurar un módulo KMP con soporte iOS.
2. Generar el framework iOS desde Gradle.
3. Inicializar AppLogger desde `iosMain` en Kotlin.
4. Verificar estado de salud del logger y problemas comunes.

---

## Prerrequisitos

| Requisito | Valor mínimo |
|---|---|
| JDK | 17 |
| Kotlin | 2.1+ |
| Gradle Wrapper | 8.10.2 |
| iOS target | iOS 14.0 |
| Módulos necesarios | `logger-core` (+ `logger-transport-supabase` si usarás Supabase) |

---

## Flujo recomendado (KMP puro)

1. Implementar la integración en Kotlin (`commonMain` + `iosMain`).
2. Configurar dependencias KMP para iOS.
3. Inicializar AppLogger en código `iosMain`.
4. Construir artefacto iOS desde Gradle.
5. Verificar logs, métricas y salud del SDK.

---

## Paso 1 — Dependencias KMP

En el módulo KMP que consume AppLogger:

```kotlin
kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.devzucca.appLoggers:logger-core:v0.1.0-alpha.1")
            }
        }

        val iosMain by getting {
            dependencies {
                // Necesario solo si vas a enviar a Supabase desde iOS
                implementation("com.github.devzucca.appLoggers:logger-transport-supabase:v0.1.0-alpha.1")
            }
        }
    }
}
```

---

## Paso 2 — Configuración segura

No hardcodees secretos en repositorio.

Ejemplo de variables locales en `local.properties` (archivo ignorado por git):

```properties
appLogger.url=https://TU-PROYECTO.supabase.co
appLogger.anonKey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
appLogger.debug=false
```

---

## Paso 3 — Inicializar desde `iosMain` (Kotlin)

Crea un archivo en `src/iosMain/kotlin/...`, por ejemplo `LoggerBootstrap.ios.kt`:

```kotlin
package com.example.app

import com.applogger.core.AppLoggerConfig
import com.applogger.core.AppLoggerIos
import com.applogger.core.AppLoggerHealth
import com.applogger.transport.supabase.SupabaseTransport

object LoggerBootstrapIos {

    fun initialize(url: String, anonKey: String, debugMode: Boolean = false) {
        val config = AppLoggerConfig.Builder()
            .endpoint(url)
            .apiKey(anonKey)
            .debugMode(debugMode)
            .batchSize(20)
            .flushIntervalSeconds(30)
            .maxStackTraceLines(50)
            .build()

        val transport = SupabaseTransport(
            endpoint = url,
            apiKey = anonKey
        )

        AppLoggerIos.shared.initialize(
            config = config,
            transport = transport
        )
    }

    fun healthSummary(): String {
        val h = AppLoggerHealth.snapshot()
        return "initialized=${h.isInitialized}, transport=${h.transportAvailable}, buffered=${h.bufferedEvents}"
    }
}
```

---

## Paso 4 — Uso del logger desde Kotlin

```kotlin
import com.applogger.core.AppLoggerIos

AppLoggerIos.shared.info("PLAYER", "Playback started")
AppLoggerIos.shared.warn("NETWORK", "Slow response", "HIGH_LATENCY")
AppLoggerIos.shared.error("PAYMENT", "Transaction failed", throwable = null)
AppLoggerIos.shared.metric("screen_load_time", 1234.0, "ms")

// Flush manual opcional
AppLoggerIos.shared.flush()
```

---

## Paso 5 — Health check (diagnóstico)

```kotlin
import com.applogger.core.AppLoggerHealth

val health = AppLoggerHealth.snapshot()
println("isInitialized=${health.isInitialized}")
println("transportAvailable=${health.transportAvailable}")
println("bufferedEvents=${health.bufferedEvents}")
println("droppedOverflow=${health.eventsDroppedDueToBufferOverflow}")
println("bufferUtilization=${health.bufferUtilizationPercentage}")
```

---

## Paso 6 — Build iOS desde Gradle

Desde `sdk/`:

```bash
./gradlew :logger-core:assembleXCFramework
```

Si tu app KMP consume el logger en su propio módulo shared, compila ese módulo para iOS con las tareas de framework correspondientes.

---

## Buenas prácticas (muy importantes)

1. Usa tags constantes (`AUTH`, `PAYMENT`, `NETWORK`) para buscar rápido en backend.
2. Nunca loguees PII (email, nombre, teléfono, dirección).
3. Nunca loguees tokens, claves o secretos.
4. Mantén `debugMode=false` fuera de desarrollo local.
5. Revisa `AppLoggerHealth.snapshot()` durante QA para detectar pérdida de eventos.

---

## Errores comunes

| Error | Causa | Solución |
|---|---|---|
| Eventos no llegan al backend | URL o key inválida | Validar `appLogger.url` y `appLogger.anonKey` |
| `production endpoint must use HTTPS` | Endpoint no seguro | Usar `https://` en producción |
| `transportAvailable=false` | Sin conectividad o backend no disponible | Ver red, DNS y estado del backend |
| `bufferedEvents` sube y no baja | Flush no ocurre o transporte falla | Revisar `flushIntervalSeconds`, conectividad y errores del transporte |

---

## Diferencias Android vs iOS (estado actual)

| Aspecto | Android | iOS |
|---|---|---|
| Entry point | `AppLoggerSDK` | `AppLoggerIos.shared` |
| Ajustes low-resource automáticos por `PlatformDetector` | Sí | No |
| `connectionType` en metadata | Detectado por `ConnectivityManager` | `"unknown"` por defecto |
| `throwable` desde host | `Throwable` Kotlin/Java | `null` cuando no hay excepción Kotlin |

---

## Estado de roadmap

- La integración recomendada en este proyecto es KMP puro.
- El flujo host nativo externo se considera legado/deprecado para nuevas implementaciones.
- Toda nueva funcionalidad debe priorizarse en código Kotlin (`commonMain`/`iosMain`).
