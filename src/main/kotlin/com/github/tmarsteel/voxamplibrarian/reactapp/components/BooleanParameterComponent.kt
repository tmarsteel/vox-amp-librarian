package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.BooleanParameter
import com.github.tmarsteel.voxamplibrarian.reactapp.IdGenerator.getUniqueId
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.label
import csstype.Auto
import csstype.Display
import csstype.TextAlign
import csstype.rem
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState

external interface BooleanParameterComponentProps : Props {
    var descriptor: BooleanParameter
    var value: Boolean
    var onValueChanged: (Boolean) -> Unit
}

val BooleanParameterComponent = FC<BooleanParameterComponentProps> { props ->
    val checkboxId by useState("boolean-parameter-" + getUniqueId())
    div {
        css {
            width = Auto.auto
            textAlign = TextAlign.center
        }

        input {
            css {
                width = 4.rem
                height = 4.rem
            }
            id = checkboxId
            type = InputType.checkbox
            checked = props.value
            onChange = { e -> props.onValueChanged(e.target.checked) }
        }
        label {
            css(classes("mt-4")) {
                display = Display.block
            }
            htmlFor = checkboxId
            +props.descriptor.id.label
        }
    }
}