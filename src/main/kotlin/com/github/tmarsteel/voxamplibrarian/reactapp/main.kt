package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.BufferedBinaryOutput
import com.github.tmarsteel.voxamplibrarian.ByteArrayBinaryInput
import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.VoxVtxAmpConnection
import com.github.tmarsteel.voxamplibrarian.parseHexStream
import com.github.tmarsteel.voxamplibrarian.protocol.TwoByteDial
import com.github.tmarsteel.voxamplibrarian.reactapp.components.SidebarComponent
import com.github.tmarsteel.voxamplibrarian.reactapp.components.SimulationConfigurationComponent
import csstype.ClassName
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import react.*
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div

val AppComponent = FC<Props> {
    var ampState: VtxAmpState? by useState(VtxAmpState.DEFAULT)

    useEffect {
        GlobalScope.launch {
            VoxVtxAmpConnection.VOX_AMP
                .flatMapLatest { it?.ampState ?: emptyFlow() }
                .collect { stateUpdate ->
                    ampState = stateUpdate
                }
        }
    }

    div {
        className = ClassName("sidebar")

        SidebarComponent {
            vtxAmpState = ampState
            onProgramSlotSelected = {
                console.log("selected $it")
            }
        }
    }

    div {
        className = ClassName("container")

        SimulationConfigurationComponent {
            configuration = ampState?.activeConfiguration ?: SimulationConfiguration.DEFAULT
            onConfigurationChanged = { newConfig ->
                ampState?.let { oldState ->
                    val newState = oldState.withActiveConfiguration(newConfig)
                    val ampConnection = VoxVtxAmpConnection.VOX_AMP.value
                    if (ampConnection == null) {
                        ampState = newState
                    } else {
                        GlobalScope.launch {
                            ampConnection.requestState(newState)
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    window.asDynamic().bullshitEncoder = bse@ { semantic: Int ->
        val out = BufferedBinaryOutput()
        TwoByteDial(semantic.toUShort()).writeTo(out)
        val input = out.copyToInput()
        return@bse input.nextByte().toString(16).padStart(2, '0') + " " + input.nextByte().toString(16).padStart(2, '0')
    }
    window.asDynamic().bullshitDecoder = bsd@ { protocol: String ->
        val input = ByteArrayBinaryInput(protocol.parseHexStream())
        return@bsd TwoByteDial.readFrom(input).semanticValue.toInt()
    }

    val rootElement = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(rootElement).render(Fragment.create {
        AppComponent {

        }
    })
}

