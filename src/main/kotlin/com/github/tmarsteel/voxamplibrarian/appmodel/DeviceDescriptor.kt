package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.MutableProgram
import com.github.tmarsteel.voxamplibrarian.protocol.PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.message.EffectPedalTypeChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToAmp

interface DeviceDescriptor {
    val name: String
    val parameters: List<DeviceParameter<*>>
    val typeChangedMessage: MessageToAmp<*>

    /**
     * @return a copy of the input [Program] where the type of this device is
     * set to `this`.
     */
    fun applyTypeToProgram(program: MutableProgram)

    fun <Value : Any> getParameter(id: DeviceParameter.Id<Value>) : DeviceParameter<Value> {
        @Suppress("UNCHECKED_CAST")
        return parameters.single { it.id == id } as DeviceParameter<Value>
    }
}

interface PedalDescriptor : DeviceDescriptor {
    val pedalType: PedalType
    override val typeChangedMessage: MessageToAmp<*>
        get() = EffectPedalTypeChangedMessage(pedalType)
}