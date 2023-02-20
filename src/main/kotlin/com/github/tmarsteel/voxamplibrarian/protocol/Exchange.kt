package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToAmp
import com.github.tmarsteel.voxamplibrarian.protocol.message.ResponseHandler

interface Exchange<Response> {
    val messagesToSend: List<MessageToAmp<*>>
    fun createResponseHandler(): ResponseHandler<Response>

    companion object {
        fun <R> of(m: MessageToAmp<R>): Exchange<R> = object : Exchange<R> {
            override val messagesToSend = listOf(m)
            override fun createResponseHandler() = m.createResponseHandler()
        }

        fun <R1, R2, RT> of(m1: MessageToAmp<R1>, m2: MessageToAmp<R2>, compose: (R1, R2) -> RT): Exchange<RT> {
            return ExchangeImpl(listOf(m1, m2)) {
                ResponseHandler.compound(m1.createResponseHandler(), m2.createResponseHandler(), compose)
            }
        }

        fun <R1, R2, R3, RT> of(m1: MessageToAmp<R1>, m2: MessageToAmp<R2>, m3: MessageToAmp<R3>, compose: (R1, R2, R3) -> RT): Exchange<RT> {
            return ExchangeImpl(listOf(m1, m2, m3)) {
                ResponseHandler.compound(
                    m1.createResponseHandler(),
                    m2.createResponseHandler(),
                    m3.createResponseHandler(),
                    compose
                )
            }
        }
    }
}

private class ExchangeImpl<Response>(
    override val messagesToSend: List<MessageToAmp<*>>,
    private val createResponseHandler: () -> ResponseHandler<Response>,
) : Exchange<Response> {
    override fun createResponseHandler() = this.createResponseHandler.invoke()
}