package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

external interface SidebarComponentProps : Props {
    var vtxAmpState: VtxAmpState?
    var onProgramSlotSelected: (ProgramSlot) -> Unit
}

val SidebarComponent = FC<SidebarComponentProps> { props ->
    val localAmpState = props.vtxAmpState

    div {
        className = classes("sidebar__inner")

        div {
            className = classes("sidebar-tree-entry", "sidebar-tree-entry--level-0")

            ConnectivityIndicatorComponent {
                isActive = props.vtxAmpState != null
            }

            span {
                className = classes("sidebar-tree-entry__label")
                +"VT20X/40X/100X Amplifier (${if (props.vtxAmpState == null) "not " else ""}connected)"
            }
        }

        for (programSlot in ProgramSlot.values()) {
            div {
                className = classes(
                    "sidebar-tree-entry",
                    "sidebar-tree-entry--level-1",
                    "sidebar-tree-entry--clickable".takeIf { props.vtxAmpState != null },
                    "sidebar-tree-entry--active".takeIf { localAmpState != null && localAmpState is VtxAmpState.ProgramSlotSelected && localAmpState.slot == programSlot }
                )

                onClick = {
                    if (props.vtxAmpState != null) {
                        props.onProgramSlotSelected(programSlot)
                    }
                }

                span {
                    className = ClassName("sidebar-tree-entry__label")
                    +"${programSlot.name}: ${props.vtxAmpState?.storedUserPrograms?.get(programSlot)?.programName ?: "<empty>"}"
                }
            }
        }
    }

    div {
        className = ClassName("sidebar__bottom")

        LogLevelComponent {}
    }

    div {
        className = classes("sidebar__actions")

        div {
            className = classes("sidebar-action", "sidebar-action--save")
            title = "Save current settings to the selected place"

            span {
                className = classes("sidebar-action__label")
                +"<"
            }
        }

        div {
            className = classes("sidebar-action", "sidebar-action--load")
            title = "Load settings from the selected place"

            span {
                className = classes("sidebar-action__label")
                +">"
            }
        }
    }
}

fun classes(vararg names: String?): ClassName = names.filterNotNull().joinToString(separator = " ").unsafeCast<ClassName>()