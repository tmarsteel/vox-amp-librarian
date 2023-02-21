package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.icon
import csstype.ClassName
import csstype.Cursor
import csstype.rem
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

external interface SidebarComponentProps : Props {
    var ampConnected: Boolean
    var vtxAmpState: VtxAmpState?
    var onProgramSlotSelected: (ProgramSlot) -> Unit
    var onSaveConfiguration: (ProgramSlot) -> Unit
    var onLoadConfiguration: (ProgramSlot) -> Unit
    var onClose: () -> Unit
}

val SidebarComponent = FC<SidebarComponentProps> { props ->
    val localAmpState = props.vtxAmpState?.takeIf { props.ampConnected }

    icon("x", "close side menu") {
        css(ClassName("sidebar-close")) {
            fontSize = 2.rem
            cursor = Cursor.pointer
        }
        onClick = { props.onClose() }
    }

    div {
        className = classes("sidebar__inner")

        div {
            className = classes("sidebar-tree-entry", "sidebar-tree-entry--level-0")

            ConnectivityIndicatorComponent {
                isActive = props.ampConnected
            }

            span {
                className = classes("sidebar-tree-entry__label")
                +"VT20X/40X/100X Amplifier (${if (!props.ampConnected) "not " else ""}connected)"
            }
        }

        for (programSlot in ProgramSlot.values()) {
            div {
                className = classes(
                    "sidebar-tree-entry",
                    "sidebar-tree-entry--level-1",
                    "sidebar-tree-entry--clickable".takeIf { props.ampConnected },
                    "sidebar-tree-entry--active".takeIf { props.ampConnected && localAmpState != null && localAmpState is VtxAmpState.ProgramSlotSelected && localAmpState.slot == programSlot }
                )

                span {
                    className = ClassName("sidebar-tree-entry__label")
                    +"${programSlot.name}: ${localAmpState?.storedUserPrograms?.get(programSlot)?.programName ?: "<empty>"}"
                    onClick = {
                        if (props.vtxAmpState != null) {
                            props.onProgramSlotSelected(programSlot)
                        }
                    }
                }

                div {
                    className = classes(
                        "sidebar-tree-entry-action",
                        "disabled".takeIf { localAmpState == null },
                    )

                    icon("upload", "Load this program")
                    onClick = {
                        if (localAmpState != null) {
                            props.onLoadConfiguration(programSlot)
                        }
                    }
                }
                div {
                    className = classes(
                        "sidebar-tree-entry-action",
                        "disabled".takeIf { localAmpState == null },
                    )

                    icon("download", "Save the current configuration to this place")
                    onClick = {
                        if (localAmpState != null) {
                            props.onSaveConfiguration(programSlot)
                        }
                    }
                }
            }
        }
    }

    div {
        className = ClassName("sidebar__bottom")

        LogLevelComponent {}
    }
}