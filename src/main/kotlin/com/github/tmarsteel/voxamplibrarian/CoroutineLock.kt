package com.github.tmarsteel.voxamplibrarian

import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

private val logger = LoggerFactory["coroutine-lock"]

/**
 * A lock that offers suspending await.
 */
class CoroutineLock(val name: String) {
    private val currentHolder: AtomicRef<Job?> = atomic(null)
    private val onReleased = Channel<Any>(Channel.BUFFERED)
    private val onReleasedObject = Any()

    suspend fun <R> withLock(block: suspend () -> R): R {
        val job = acquireForCurrentJob()
        try {
            return block()
        }
        finally {
            release(job)
        }
    }

    private suspend fun acquireForCurrentJob(): Job {
        val currentJob = currentCoroutineContext().job
        retry@ while (true) {
            if (currentHolder.compareAndSet(null, currentJob)) {
                clearReleasedSignal()
                return currentJob
            }

            onReleased.receive()
        }
    }

    private suspend fun release(forJob: Job) {
        check(currentHolder.compareAndSet(forJob, null)) {
            "Cannot release lock for job $forJob because its held by another job. Coding error."
        }
        onReleased.send(onReleasedObject)
    }

    private fun clearReleasedSignal() {
        while(true) {
            val result = onReleased.tryReceive()
            if (result.isFailure || result.isClosed) {
                return
            }
        }
    }
}