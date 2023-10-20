package com.github.tmarsteel.voxamplibrarian.reactapp.components.sidebar

import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.icon
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

external interface ProgramSlotComponentProps : Props {
    var location: ProgramSlotLocation
    var programName: String?
    var onViewProgram: (() -> Unit)?
    var onSaveToThisLocation: (() -> Unit)?
    var onActivated: (() -> Unit)?
    var onSaveIntoSelectedAmpSlot: (() -> Unit)?
    var isActive: Boolean
}

sealed class ProgramSlotLocation {
    class Amplifier(val slot: ProgramSlot) : ProgramSlotLocation()
    class File(val filename: String?, val index: Int) : ProgramSlotLocation()
}

val ProgramSlotComponent = FC<ProgramSlotComponentProps> { props ->
    div {
        className = classes(
            "program-slot",
            "activatable".takeIf { props.onActivated != null },
            "active".takeIf { props.isActive },
        )
        onClick = { props.onActivated?.invoke() }

        div {
            className = classes("program-slot__title")

            val displayName = props.programName?.takeUnless { it.isBlank() }
            span {
                className = classes(
                    "program-slot__name",
                    "empty".takeIf { displayName == null },
                )

                +(displayName ?: "<no name>")
            }

            div {
                className = classes("program-slot__location")

                val location = props.location
                when (location) {
                    is ProgramSlotLocation.Amplifier -> {
                        icon("speaker-fill", "VOX VTX Amplifier")
                        span {
                            +(location.slot.name)
                        }
                    }

                    is ProgramSlotLocation.File -> {
                        icon("file-earmark", "${location.filename ?: "<unnamed file>"}, #${location.index + 1}")
                    }
                }
            }
        }

        div {
            className = classes("actions")

            props.onViewProgram?.let { callback ->
                button {
                    icon("eye", "Loads the settings of this program into the view")
                    +"View & Edit"
                    title="Loads the settings of this program into the view"
                    onClick = { event -> event.stopPropagation(); callback() }
                }
            }

            props.onSaveToThisLocation?.let { callback ->
                button {
                    icon("download", "Saves the settings in the view into this location")
                    +"Save here"
                    title="Saves the settings in the view into this location"
                    onClick = { event -> event.stopPropagation(); callback() }
                }
            }

            props.onSaveIntoSelectedAmpSlot?.let { callback ->
                button {
                    icon("upload", "Writes this program to the selected slot on the amp")
                    +"Save to Amp slot"
                    title = "Writes this program to the selected slot on the amp"
                    onClick = { event -> event.preventDefault(); callback() }
                }
            }
        }
    }
}