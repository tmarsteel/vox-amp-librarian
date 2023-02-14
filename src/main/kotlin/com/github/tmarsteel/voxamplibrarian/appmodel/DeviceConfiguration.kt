package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.ParameterValue.Companion.withValue
import com.github.tmarsteel.voxamplibrarian.protocol.MutableProgram

class DeviceConfiguration<out D : DeviceDescriptor> private constructor(
    val descriptor: D,
    private val values: Map<DeviceParameter.Id<*>, Any>,
) {
    constructor(descriptor: D, values: List<ParameterValue<*>>) : this(
        descriptor,
        values.associate { it.parameterId to it.value },
    )

    init {
        val missingParameterIds = descriptor.parameters.asSequence().map { it.id }.filter { it !in this.values }.toList()
        if (missingParameterIds.isNotEmpty()) {
            throw IllegalArgumentException("Missing parameters ${missingParameterIds.joinToString()} for ${descriptor.name}")
        }
    }

    fun rejectInvalid() {
        for (parameter in descriptor.parameters) {
            check(parameter.id in values)
            @Suppress("UNCHECKED_CAST")
            (parameter as DeviceParameter<in Any>).rejectInvalidValue(values[parameter.id]!!)
        }
    }

    fun <V : Any> getValue(parameterId: DeviceParameter.Id<V>): V {
        val untyped = values[parameterId] ?: throw RuntimeException("Device $descriptor does not have parameter $parameterId")
        @Suppress("UNCHECKED_CAST")
        return untyped as V
    }

    fun <V : Any> withValue(param: DeviceParameter.Id<V>, value: V): DeviceConfiguration<D> {
        val newValues = values.toMutableMap()
        newValues[param] = value
        return DeviceConfiguration(descriptor, newValues)
    }

    fun <ND : DeviceDescriptor> withDescriptor(newDescriptor: ND): DeviceConfiguration<ND> {
        val newValues = newDescriptor.parameters
            .associate { it.id to (values[it.id] ?: it.default) }

        return DeviceConfiguration(newDescriptor, newValues)
    }

    fun applyToProgram(program: MutableProgram) {
        descriptor.applyTypeToProgram(program)
        descriptor.parameters.forEach {
            @Suppress("UNCHECKED_CAST")
            it as DeviceParameter<in Any>
            it.protocolAdapter.applyToProgram(program, values.getValue(it.id))
        }
    }

    companion object {
        fun <D : DeviceDescriptor> defaultOf(descriptor: D): DeviceConfiguration<D> = DeviceConfiguration(
            descriptor,
            descriptor.parameters.map {
                @Suppress("UNCHECKED_CAST")
                it as DeviceParameter<Any>
                it.id.withValue(it.default)
            }
        )
    }
}