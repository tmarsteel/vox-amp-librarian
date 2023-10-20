package com.github.tmarsteel.voxamplibrarian.reactapp.components.sidebar

import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.icon
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

external interface AddProgramSlotComponentProps : Props {
    var onAddSlot: () -> Unit
}

val AddProgramSlotComponent = FC<AddProgramSlotComponentProps> { props ->
    div {
        className = classes(
            "program-slot",
            "activatable",
        )
        onClick = { props.onAddSlot() }

        icon("plus-circle-dotted")
    }
}