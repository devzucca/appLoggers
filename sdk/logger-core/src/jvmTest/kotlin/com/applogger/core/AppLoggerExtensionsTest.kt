package com.applogger.core

import com.applogger.core.model.LogLevel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppLoggerExtensionsTest {

    // ── Minimal in-test spy, avoids logger-test circular dep ──────────────────

    data class Capture(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null,
        val extra: Map<String, Any>? = null
    )

    class SpyLogger : AppLogger {
        val calls = mutableListOf<Capture>()
        override fun debug(tag: String, message: String, throwable: Throwable?, extra: Map<String, Any>?) =
            calls.add(Capture(LogLevel.DEBUG, tag, message, throwable, extra)).let {}
        override fun info(tag: String, message: String, throwable: Throwable?, extra: Map<String, Any>?) =
            calls.add(Capture(LogLevel.INFO, tag, message, throwable, extra)).let {}
        override fun warn(tag: String, message: String, throwable: Throwable?, anomalyType: String?, extra: Map<String, Any>?) =
            calls.add(Capture(LogLevel.WARN, tag, message, throwable, extra)).let {}
        override fun error(tag: String, message: String, throwable: Throwable?, extra: Map<String, Any>?) =
            calls.add(Capture(LogLevel.ERROR, tag, message, throwable, extra)).let {}
        override fun critical(tag: String, message: String, throwable: Throwable?, extra: Map<String, Any>?) =
            calls.add(Capture(LogLevel.CRITICAL, tag, message, throwable, extra)).let {}
        override fun metric(name: String, value: Double, unit: String, tags: Map<String, String>?) = Unit
        override fun flush() = Unit
    }

    private lateinit var spy: SpyLogger

    @BeforeEach
    fun setup() {
        spy = SpyLogger()
    }

    // ── logTag() ──────────────────────────────────────────────────────────────

    @Test
    fun `logTag returns simple class name`() {
        assertEquals("AppLoggerExtensionsTest", this.logTag())
    }

    @Test
    fun `logTag on anonymous object returns Anonymous`() {
        val anon = object : Any() {}
        assertEquals("Anonymous", anon.logTag())
    }

    // ── AppLogger shorthand extensions ────────────────────────────────────────

    @Test
    fun `logD delegates to debug with correct tag and message`() {
        spy.logD("TAG", "debug message")
        assertEquals(1, spy.calls.size)
        spy.calls[0].also {
            assertEquals(LogLevel.DEBUG, it.level)
            assertEquals("TAG", it.tag)
            assertEquals("debug message", it.message)
            assertNull(it.throwable)
        }
    }

    @Test
    fun `logI delegates to info with throwable`() {
        val e = RuntimeException("info-error")
        spy.logI("TAG", "info message", throwable = e)
        assertEquals(LogLevel.INFO, spy.calls[0].level)
        assertEquals(e, spy.calls[0].throwable)
    }

    @Test
    fun `logW delegates to warn with anomalyType and throwable`() {
        val e = Exception("slow")
        spy.logW("NETWORK", "Slow response", throwable = e, anomalyType = "HIGH_LATENCY")
        assertEquals(LogLevel.WARN, spy.calls[0].level)
        assertEquals(e, spy.calls[0].throwable)
    }

    @Test
    fun `logE delegates to error`() {
        val e = RuntimeException("payment failed")
        spy.logE("PAYMENT", "Transaction error", throwable = e)
        assertEquals(LogLevel.ERROR, spy.calls[0].level)
        assertEquals("payment failed", spy.calls[0].throwable?.message)
    }

    @Test
    fun `logC delegates to critical`() {
        spy.logC("AUTH", "Token refresh failed")
        assertEquals(LogLevel.CRITICAL, spy.calls[0].level)
        assertEquals("AUTH", spy.calls[0].tag)
    }

    @Test
    fun `logD passes extra map`() {
        spy.logD("TAG", "msg", extra = mapOf("key" to "value"))
        assertEquals("value", spy.calls[0].extra?.get("key"))
    }

    // ── Any tag-inferring extensions ──────────────────────────────────────────

    @Test
    fun `Any logD infers tag from class name`() {
        this.logD(spy, "debug via any")
        assertEquals("AppLoggerExtensionsTest", spy.calls[0].tag)
        assertEquals("debug via any", spy.calls[0].message)
        assertEquals(LogLevel.DEBUG, spy.calls[0].level)
    }

    @Test
    fun `Any logI infers tag and captures throwable`() {
        val e = RuntimeException("any-info-error")
        this.logI(spy, "info via any", throwable = e)
        assertEquals("AppLoggerExtensionsTest", spy.calls[0].tag)
        assertEquals(e, spy.calls[0].throwable)
    }

    @Test
    fun `Any logW infers tag with anomalyType and throwable`() {
        val e = Exception("warn via any")
        this.logW(spy, "warn message", throwable = e, anomalyType = "ANOMALY")
        assertEquals("AppLoggerExtensionsTest", spy.calls[0].tag)
        assertEquals(e, spy.calls[0].throwable)
        assertEquals(LogLevel.WARN, spy.calls[0].level)
    }

    @Test
    fun `Any logE infers tag and records error`() {
        val e = RuntimeException("error via any")
        this.logE(spy, "error message", throwable = e)
        assertEquals("AppLoggerExtensionsTest", spy.calls[0].tag)
        assertEquals(e, spy.calls[0].throwable)
        assertEquals(LogLevel.ERROR, spy.calls[0].level)
    }

    @Test
    fun `Any logC infers tag and records critical`() {
        this.logC(spy, "critical message")
        assertEquals("AppLoggerExtensionsTest", spy.calls[0].tag)
        assertEquals("critical message", spy.calls[0].message)
        assertEquals(LogLevel.CRITICAL, spy.calls[0].level)
    }

    @Test
    fun `Any logD with extra map passes metadata`() {
        this.logD(spy, "with extra", extra = mapOf("key" to "value"))
        assertEquals("value", spy.calls[0].extra?.get("key"))
    }

    @Test
    fun `null throwable results in null in captured call`() {
        this.logI(spy, "no exception", throwable = null)
        assertNull(spy.calls[0].throwable)
    }
}
