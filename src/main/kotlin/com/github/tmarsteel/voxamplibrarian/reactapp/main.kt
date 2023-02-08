package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.*
import com.github.tmarsteel.voxamplibrarian.reactapp.components.DeviceComponent
import com.github.tmarsteel.voxamplibrarian.reactapp.components.DeviceSlotComponent
import com.github.tmarsteel.voxamplibrarian.reactapp.components.DeviceTypeSelectorComponent
import com.github.tmarsteel.voxamplibrarian.reactapp.components.SimulationConfigurationComponent
import csstype.ClassName
import kotlinx.browser.document
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.useState

val AppComponent = FC<Props> {
    var simulationConfig by useState(SimulationConfiguration.DEFAULT)

    div {
        className = ClassName("container")

        SimulationConfigurationComponent {
            configuration = simulationConfig
            onConfigurationChanged = { newConfig ->
                simulationConfig = newConfig
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

