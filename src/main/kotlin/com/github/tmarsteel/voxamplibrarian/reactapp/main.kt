package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.AmplifierDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import com.github.tmarsteel.voxamplibrarian.reactapp.components.DeviceComponent
import csstype.ClassName
import kotlinx.browser.document
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.useState

val AppComponent = FC<Props> {
    var ampConfig by useState(AmplifierDescriptor.DEFAULT)
    div {
        className = ClassName("container")
        DeviceComponent {
            configuration = ampConfig
            onValueChanged = { param, newValue ->
                ampConfig = ampConfig.withValue(param as DeviceParameter<Any>, newValue)
            }
        }
    }
}

fun main() {
    val rootElement = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(rootElement).render(Fragment.create {
        AppComponent {

        }
    })
}

private fun <D : DeviceDescriptor, T : Any> DeviceConfiguration<D>.withValue(parameter: DeviceParameter<T>, newValue: T): DeviceConfiguration<D> {
    return copy(
        values = values.toMutableMap()
            .also {
                it[parameter.id] = newValue
            }
    )
}