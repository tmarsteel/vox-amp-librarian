package com.github.tmarsteel.voxamplibrarian.appmodel

class ParameterValue<V : Any>(
    val parameter: DeviceParameter<V>,
    val value: V,
) {
    companion object {
        infix fun <V : Any> DeviceParameter<V>.withValue(value: V) = ParameterValue(this, value)
    }
}