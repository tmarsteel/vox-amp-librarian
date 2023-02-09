package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.VoxVtxAmpConnection
import com.github.tmarsteel.voxamplibrarian.reactapp.components.SimulationConfigurationComponent
import csstype.ClassName
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import react.*
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div

val AppComponent = FC<Props> {
    var simulationConfig by useState(SimulationConfiguration.DEFAULT)
    useEffect {
        GlobalScope.launch {
            VoxVtxAmpConnection.VOX_AMP
                .flatMapLatest { it?.ampState ?: emptyFlow() }
                .collect {
                    simulationConfig = it.configuration
                }
        }
    }

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

