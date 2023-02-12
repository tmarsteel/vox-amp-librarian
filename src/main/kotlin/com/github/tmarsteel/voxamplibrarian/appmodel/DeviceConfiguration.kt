package com.github.tmarsteel.voxamplibrarian.appmodel

class DeviceConfiguration<out D : DeviceDescriptor> private constructor(
    val descriptor: D,
    private val values: Map<DeviceParameter.Id<*>, Any>,
) {
    constructor(descriptor: D, values: List<ParameterValue<*>>) : this(
        descriptor,
        values.associate { it.parameter.id to it.value },
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

    fun <V : Any> withValue(param: DeviceParameter.Id<V>, value: V): DeviceConfiguration<D> {
        val newValues = values.toMutableMap()
        newValues[param] = value
        return DeviceConfiguration(descriptor, newValues)
    }

    fun <ND : DeviceDescriptor> withDescriptor(newDescriptor: ND): DeviceConfiguration<ND> {
        val newValues = newDescriptor.parameters
            .map { it.id }
            .associateWith { id ->
                values[id] ?: newDescriptor.getDefaultValue(id)
            }

        return DeviceConfiguration(newDescriptor, newValues)
    }

    companion object {
        fun <D : DeviceDescriptor> defaultOf(descriptor: D): DeviceConfiguration<D> = DeviceConfiguration(
            descriptor,
            descriptor.parameters.map { descriptor.getDefaultValue(it.id) },
        )
    }
}