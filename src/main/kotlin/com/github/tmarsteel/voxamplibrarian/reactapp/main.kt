package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.VoxVtxAmpConnection
import com.github.tmarsteel.voxamplibrarian.reactapp.components.LogLevelComponent
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

private val startupCode = mutableListOf<() -> Unit>()
private var appInitStarted = false
fun appInit(block: () -> Unit) {
    if (appInitStarted) {
        block()
    } else {
        startupCode.add(block)
    }
}

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
        div {
            className = ClassName("row justify-content-end")
            div {
                className = ClassName("col-2")
                LogLevelComponent {}
            }
        }
        SimulationConfigurationComponent {
            configuration = simulationConfig
            onConfigurationChanged = { newConfig ->
                simulationConfig = newConfig
            }
        }
    }
}

fun main() {
    appInitStarted = true
    startupCode.forEach {
        it.invoke()
    }
    val rootElement = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(rootElement).render(Fragment.create {
        AppComponent {

        }
    })
}

