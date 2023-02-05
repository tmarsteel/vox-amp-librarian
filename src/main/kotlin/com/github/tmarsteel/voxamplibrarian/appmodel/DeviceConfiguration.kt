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
}