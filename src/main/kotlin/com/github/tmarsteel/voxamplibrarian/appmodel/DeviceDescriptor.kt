package com.github.tmarsteel.voxamplibrarian.appmodel

abstract class DeviceDescriptor(
    val name: String,
    defaults: List<ParameterValue<*>>,
) {
    val parameters: List<DeviceParameter<*>> = defaults.map { it.parameter }
    private val defaults: Map<DeviceParameter.Id<*>, ParameterValue<*>> = defaults.associateBy { it.parameter.id }

    fun <V : Any> getDefaultValue(parameterId: DeviceParameter.Id<V>): ParameterValue<V> {
        val value = defaults[parameterId]
            ?: throw RuntimeException("No default defined for parameter $parameterId")

        @Suppress("UNCHECKED_CAST")
        return value as ParameterValue<V>
    }
}