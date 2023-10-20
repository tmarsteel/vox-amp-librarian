package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.VoxVtxAmpConnection
import com.github.tmarsteel.voxamplibrarian.installPolyfills
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.reactapp.components.SimulationConfigurationComponent
import com.github.tmarsteel.voxamplibrarian.reactapp.components.sidebar.SidebarComponent
import com.github.tmarsteel.voxamplibrarian.useEffectCoroutine
import csstype.ClassName
import csstype.Cursor
import csstype.rem
import emotion.react.css
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import react.*
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div

private val logger = LoggerFactory["main"]

val AppComponent = FC<Props> {
    var ampState: VtxAmpState? by useState(VtxAmpState.DEFAULT)
    var ampConnected: Boolean by useState(false)
    var nonAmpConfigForViewing: SimulationConfiguration? by useState(null)

    var sidebarExplicitlyOpen: Boolean by useState(false)

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
        className = classes("topbar")

        div {
            className = classes("container-xxl-fluid")

            div {
                className = classes("row")

                div {
                    className = classes("col-12")

                    icon("list", "open menu") {
                        css(ClassName("sidebar-open")) {
                            fontSize = 2.rem
                            cursor = Cursor.pointer
                        }
                        onClick = {
                            sidebarExplicitlyOpen = !sidebarExplicitlyOpen
                        }
                    }
                }
            }
        }
    }

    div {
        className = classes(
            "sidebar",
            "open".takeIf { sidebarExplicitlyOpen },
        )

        SidebarComponent {
            this.ampConnected = ampConnected
            vtxAmpState = ampState
            onProgramSlotSelected = {
                VoxVtxAmpConnection.VOX_AMP.value?.selectUserProgramSlot(it)
                nonAmpConfigForViewing = null
            }
            onLoadConfiguration = load@{ fromSlot ->
                val localAmpState = ampState
                val ampConnection = VoxVtxAmpConnection.VOX_AMP.value
                if (localAmpState == null || ampConnection == null) {
                    window.alert("Amplifier not connected")
                    return@load
                }

                ampConnection.requestState(localAmpState.withActiveConfiguration(localAmpState.storedUserPrograms.getValue(fromSlot)))
            }
            onSaveConfiguration = save@{ toSlot ->
                val localAmpState = ampState ?: return@save
                VoxVtxAmpConnection.VOX_AMP.value?.persistConfigurationToSlot(localAmpState.activeConfiguration, toSlot)
            }
            onViewNonAmpConfiguration = {
                nonAmpConfigForViewing = it
            }
            onWriteConfigurationToAmpSlot = save@{ config, targetSlot ->
                val ampConnection = VoxVtxAmpConnection.VOX_AMP.value ?: return@save
                val localAmpState = ampState ?: return@save
                if (localAmpState is VtxAmpState.ProgramSlotSelected && localAmpState.slot == targetSlot && localAmpState.storedUserPrograms[localAmpState.slot] != localAmpState.activeConfiguration) {
                    if (!window.confirm("You have unsaved changes for slot $targetSlot, continue?")) {
                        return@save
                    }
                }

                ampConnection.persistConfigurationToSlot(config, targetSlot)
            }
            onClose = {
                sidebarExplicitlyOpen = false
            }
        }
    }

    div {
        className = classes("simulation-config")

        div {
            className = classes("container-xxl-fluid")

            SimulationConfigurationComponent {
                configuration = nonAmpConfigForViewing ?: ampState?.activeConfiguration ?: SimulationConfiguration.DEFAULT
                onConfigurationChanged = configChanged@{ newConfig ->
                    if (nonAmpConfigForViewing != null) {
                        return@configChanged
                    }

                    ampState?.let { oldState ->
                        val newState = oldState.withActiveConfiguration(newConfig)
                        val ampConnection = VoxVtxAmpConnection.VOX_AMP.value
                        if (ampConnection == null) {
                            ampState = newState
                        } else {
                            ampConnection.requestState(newState)
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    installPolyfills()
    val rootElement = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(rootElement).render(Fragment.create {
        AppComponent {

        }
    })
}

