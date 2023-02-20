package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import csstype.ClassName
import react.ChildrenBuilder
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.span

external interface SidebarComponentProps : Props {
    var ampConnected: Boolean
    var vtxAmpState: VtxAmpState?
    var onProgramSlotSelected: (ProgramSlot) -> Unit
    var onSaveConfiguration: () -> Unit
    var onLoadConfiguration: () -> Unit
}

val SidebarComponent = FC<SidebarComponentProps> { props ->
    val localAmpState = props.vtxAmpState?.takeIf { props.ampConnected }

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
                    className = ClassName("sidebar-tree-entry-action")

                    icon("upload", "Load this program")
                }
                div {
                    className = ClassName("sidebar-tree-entry-action")

                    icon("download", "Save the current configuration to this place")
                }
            }
        }
    }

    div {
        className = ClassName("sidebar__bottom")

        LogLevelComponent {}
    }
}

fun classes(vararg names: String?): ClassName = names.filterNotNull().joinToString(separator = " ").unsafeCast<ClassName>()

fun ChildrenBuilder.icon(name: String, alt: String, vararg classes: String) {
    img {
        className = classes("icon", *classes)
        src = "/icons/$name.svg"
        this.alt = alt
        title = alt
    }
}