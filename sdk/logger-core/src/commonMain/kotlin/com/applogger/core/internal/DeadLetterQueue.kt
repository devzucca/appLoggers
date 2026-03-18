package com.applogger.core.internal

import com.applogger.core.currentTimeMillis
import com.applogger.core.model.LogEvent

/**
 * In-memory dead letter queue for events that exhaust all retry attempts.
 *
 * Bounded to [maxCapacity] events (FIFO eviction). Thread-safe.
 */
internal class DeadLetterQueue(
    private val maxCapacity: Int = 200
) {
    private val queue = ArrayDeque<FailedEvent>(maxCapacity)
    private val lock = Any()

    /**
     * Adds a failed batch to the DLQ.
     * Oldest events are evicted if the queue is at capacity.
     */
    fun enqueue(events: List<LogEvent>, reason: String) = platformSynchronized(lock) {
        val timestamp = currentTimeMillis()
        events.forEach { event ->
            if (queue.size >= maxCapacity) {
                queue.removeFirst()
            }
            queue.addLast(FailedEvent(event, reason, timestamp))
        }
    }

    /** Returns all dead-lettered events and clears the queue. */
    fun drain(): List<FailedEvent> = platformSynchronized(lock) {
        val result = queue.toList()
        queue.clear()
        result
    }

    /** Returns the current count of dead-lettered events. */
    fun size(): Int = platformSynchronized(lock) { queue.size }

    /** Returns `true` if the DLQ is empty. */
    fun isEmpty(): Boolean = platformSynchronized(lock) { queue.isEmpty() }

    data class FailedEvent(
        val event: LogEvent,
        val reason: String,
        val failedAt: Long
    )
}
