package com.github.tmarsteel.voxamplibrarian.appmodel

class ParameterValue<V : Any>(
    val parameterId: DeviceParameter.Id<V>,
    val value: V,
) {
    companion object {
        infix fun <V : Any> DeviceParameter.Id<V>.withValue(value: V) = ParameterValue(this, value)
    }
}