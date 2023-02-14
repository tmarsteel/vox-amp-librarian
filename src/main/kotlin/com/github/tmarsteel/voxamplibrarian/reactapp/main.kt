package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.VoxVtxAmpConnection
import com.github.tmarsteel.voxamplibrarian.reactapp.components.LogLevelComponent
import com.github.tmarsteel.voxamplibrarian.reactapp.components.SimulationConfigurationComponent
import csstype.ClassName
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState
import kotlin.reflect.KProperty

private val startupCode = mutableListOf<() -> Unit>()
private var appInitStarted = false
fun appInit(block: () -> Unit) {
    if (appInitStarted) {
        block()
    } else {
        startupCode.add(block)
    }
}

class ValueDelegate<T>(private val obtain: () -> T, private val set: (T) -> Unit) {
    operator fun getValue(
        thisRef: Nothing?,
        property: KProperty<*>,
    ): T = obtain()

    operator fun setValue(
        thisRef: Nothing?,
        property: KProperty<*>,
        value: T,
    ) {
        set(value)
    }
}

fun useAmpState(source: StateFlow<VoxVtxAmpConnection?>): ValueDelegate<VtxAmpState?> {
    var ampStateInstance: VtxAmpState? by useState(null)

    val delegate = ValueDelegate(
        obtain = { ampStateInstance },
        set = { newState ->
            if (newState == null) {
                return@ValueDelegate
            }

            GlobalScope.launch {
                source.value?.setState(newState)
                ampStateInstance = newState
            }
        }
    )

    useEffect {
        GlobalScope.launch {
            source
                .flatMapLatest { it?.ampState ?: emptyFlow() }
                .collect {
                    delegate.setValue(null, String::length, it)
                }
        }
    }

    return delegate
}

val AppComponent = FC<Props> {
    var ampState by useAmpState(VoxVtxAmpConnection.VOX_AMP)

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
            configuration = ampState?.configuration ?: SimulationConfiguration.DEFAULT
            onConfigurationChanged = { newConfig ->
                ampState?.let { oldState ->
                    ampState = oldState.withConfiguration(newConfig)
                }
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

