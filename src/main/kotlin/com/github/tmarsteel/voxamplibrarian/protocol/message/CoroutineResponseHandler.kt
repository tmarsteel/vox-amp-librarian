package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.*

class CoroutineResponseHandler<R> internal constructor(
    block: suspend CoroutineResponseHandler<R>.Scope.() -> R
) : ResponseHandler<R> {
    private var onNextMessageContinuation: Continuation<BinaryInput>? = null
    private var result: Result<R>? = null
    private val completionContinuation: Continuation<R> = object : Continuation<R> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<R>) {
            this@CoroutineResponseHandler.result = result
        }
    }

    init {
        block.startCoroutine(Scope(), completionContinuation)
    }

    override fun onMessage(payload: BinaryInput): ResponseHandler.MessageResult<R> {
        val localContinuation = onNextMessageContinuation
            ?: throw IllegalStateException("Cannot consume this message because ${this::onNextMessageContinuation.name} is not set.")

        localContinuation.resumeWith(Result.success(payload))
        return result?.getOrThrow()
            ?.let { ResponseHandler.MessageResult.ResponseComplete(it) }
            ?: ResponseHandler.MessageResult.MoreMessagesNeeded
    }

    override fun cancel() {
        onNextMessageContinuation?.resumeWithException(CancellationException("Response consumption cancelled"))
    }

    @RestrictsSuspension
    inner class Scope {
        suspend fun receiveNext(): BinaryInput {
            return suspendCancellableCoroutine {
                onNextMessageContinuation = it
            }
        }

        suspend fun <SubResult> subProcess(handler: ResponseHandler<SubResult>): SubResult {
            return suspendCancellableCoroutine { continuationForSubResult ->
                onNextMessageContinuation = object : Continuation<BinaryInput> {
                    override val context = continuationForSubResult.context

                    override fun resumeWith(result: Result<BinaryInput>) {
                        if (result.isFailure) {
                            @Suppress("UNCHECKED_CAST")
                            continuationForSubResult.resumeWith(result as Result<SubResult>)
                            return
                        }

                        val messageSubResult = handler.onMessage(result.getOrThrow())
                        if (messageSubResult is ResponseHandler.MessageResult.ResponseComplete) {
                            continuationForSubResult.resumeWith(Result.success(messageSubResult.response))
                        }
                    }
                }
            }
        }
    }
}