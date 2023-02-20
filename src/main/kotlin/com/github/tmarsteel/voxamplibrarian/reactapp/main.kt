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
import com.github.tmarsteel.voxamplibrarian.useEffectCoroutine
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
    var ampConnected: Boolean by useState(false)

    useEffectCoroutine {
        VoxVtxAmpConnection.VOX_AMP
            .flatMapLatest {
                if (it != null) {
                    ampConnected = true
                    it.ampState
                } else {
                    ampConnected = false
                    emptyFlow()
                }
            }
            .collect { stateUpdate ->
                ampState = stateUpdate
            }
    }

    div {
        className = ClassName("sidebar")

        SidebarComponent {
            this.ampConnected = ampConnected
            vtxAmpState = ampState
            onProgramSlotSelected = {
                GlobalScope.launch {
                    VoxVtxAmpConnection.VOX_AMP.value?.selectUserProgramSlot(it)
                }
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

