package com.github.tmarsteel.voxamplibrarian.appmodel

data class DeviceConfiguration<out D : DeviceDescriptor>(
    val descriptor: D,
    val values: Map<DeviceParameter.Id, Any>,
) {
    fun rejectInvalid() {
        for (parameter in descriptor.parameters) {
            check(parameter.id in values)
            parameter.rejectInvalidValue(values[parameter.id]!!)
        }
    }

    fun withValue(param: DeviceParameter.Id, value: Any): DeviceConfiguration<D> {
        val newValues = values.toMutableMap()
        newValues[param] = value
        return DeviceConfiguration(descriptor, newValues)
    }

    fun <ND : DeviceDescriptor> withDescriptor(newDescriptor: ND): DeviceConfiguration<ND> {
        val newValues = newDescriptor.parameters
            .map { it.id }
            .associateWith { id -> values[id] ?: newDescriptor.defaults.getValue(id) }

        return DeviceConfiguration(newDescriptor, newValues)
    }
}