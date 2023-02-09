package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.midiState
import com.github.tmarsteel.voxamplibrarian.reactapp.components.SimulationConfigurationComponent
import csstype.ClassName
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import react.*
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div

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
    GlobalScope.launch {
        midiState.collect {
            console.log(it)
        }
    }
    val rootElement = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(rootElement).render(Fragment.create {
        AppComponent {

        }
    })
}

