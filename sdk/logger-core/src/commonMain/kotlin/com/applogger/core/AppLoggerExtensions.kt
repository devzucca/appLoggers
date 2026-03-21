package com.applogger.core

/**
 * Kotlin extension functions for [AppLogger] that infer the tag from the calling class name.
 *
 * These helpers reduce boilerplate in classes that already hold a logger reference.
 * The tag is derived from the simple class name of the receiver.
 *
 * ## Usage
 * ```kotlin
 * class PlayerController(private val logger: AppLogger) {
 *     fun start() {
 *         logger.logI("PLAYER", "Playback started")
 *         logger.logW("PLAYER", "Buffer low", anomalyType = "BUFFER_LOW")
 *         logger.logE("PLAYER", "Playback failed", throwable = e)
 *     }
 * }
 * ```
 *
 * ## Against-the-receiver style (tag auto-inferred as class name)
 * ```kotlin
 * class AuthRepository(private val logger: AppLogger) {
 *     fun login() {
 *         this.logD(logger, "Login attempt")
 *         this.logE(logger, "Login failed", throwable = e)
 *     }
 * }
 * ```
 *
 * @see AppLogger for the full low-level API.
 */

// ─── Convenience methods  on AppLogger (no tag inference) ─────────────────────

/** Logs a debug message. Shorthand for [AppLogger.debug]. */
fun AppLogger.logD(tag: String, message: String, throwable: Throwable? = null, extra: Map<String, Any>? = null) =
    debug(tag, message, throwable, extra)

/** Logs an info message. Shorthand for [AppLogger.info]. */
fun AppLogger.logI(tag: String, message: String, throwable: Throwable? = null, extra: Map<String, Any>? = null) =
    info(tag, message, throwable, extra)

/** Logs a warning. Shorthand for [AppLogger.warn]. */
fun AppLogger.logW(
    tag: String,
    message: String,
    throwable: Throwable? = null,
    anomalyType: String? = null,
    extra: Map<String, Any>? = null
) = warn(tag, message, throwable, anomalyType, extra)

/** Logs an error. Shorthand for [AppLogger.error]. */
fun AppLogger.logE(tag: String, message: String, throwable: Throwable? = null, extra: Map<String, Any>? = null) =
    error(tag, message, throwable, extra)

/** Logs a critical/fatal event. Shorthand for [AppLogger.critical]. */
fun AppLogger.logC(tag: String, message: String, throwable: Throwable? = null, extra: Map<String, Any>? = null) =
    critical(tag, message, throwable, extra)

// ─── Tag-inferring extensions on Any ──────────────────────────────────────────

/**
 * Returns a safe tag derived from the simple class name of the receiver,
 * suitable for use as an [AppLogger] tag.
 *
 * Anonymous or lambda classes return `"Anonymous"`. Tags longer than 100
 * characters are truncated (SDK limit).
 */
fun Any.logTag(): String =
    this::class.simpleName?.take(100) ?: "Anonymous"

/** Logs a debug message, inferring the tag from the receiver's class name. */
fun Any.logD(logger: AppLogger, message: String, throwable: Throwable? = null, extra: Map<String, Any>? = null) =
    logger.debug(logTag(), message, throwable, extra)

/** Logs an info message, inferring the tag from the receiver's class name. */
fun Any.logI(logger: AppLogger, message: String, throwable: Throwable? = null, extra: Map<String, Any>? = null) =
    logger.info(logTag(), message, throwable, extra)

/** Logs a warning, inferring the tag from the receiver's class name. */
fun Any.logW(
    logger: AppLogger,
    message: String,
    throwable: Throwable? = null,
    anomalyType: String? = null,
    extra: Map<String, Any>? = null
) = logger.warn(logTag(), message, throwable, anomalyType, extra)

/** Logs an error, inferring the tag from the receiver's class name. */
fun Any.logE(logger: AppLogger, message: String, throwable: Throwable? = null, extra: Map<String, Any>? = null) =
    logger.error(logTag(), message, throwable, extra)

/** Logs a critical event, inferring the tag from the receiver's class name. */
fun Any.logC(logger: AppLogger, message: String, throwable: Throwable? = null, extra: Map<String, Any>? = null) =
    logger.critical(logTag(), message, throwable, extra)
