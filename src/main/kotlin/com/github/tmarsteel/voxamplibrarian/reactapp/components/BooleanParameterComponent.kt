package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.BooleanParameter
import com.github.tmarsteel.voxamplibrarian.reactapp.IdGenerator.getUniqueId
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.icon
import com.github.tmarsteel.voxamplibrarian.reactapp.label
import csstype.*
import emotion.react.css
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.createRef
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
    val checkboxRef = createRef<HTMLInputElement>()
    div {
        css(classes("boolean-parameter")) {
            width = Auto.auto
            textAlign = TextAlign.center
        }

        div {
            css(classes(
                "boolean-parameter__checkbox-display",
                "boolean-parameter__checkbox-display--checked".takeIf { props.value },
            )) {
                height = 4.rem
                width = 4.rem
                marginLeft = Auto.auto
                marginRight = Auto.auto
            }

            icon("check2", "enabled") {
                css {
                    height = 4.rem
                    width = 4.rem
                }
            }
        }
        input {
            css {
                width = 4.rem
                height = 4.rem
                opacity = number(0.0)
                marginTop = (-4).rem
                display = Display.block
                marginLeft = Auto.auto
                marginRight = Auto.auto
            }
            id = checkboxId
            type = InputType.checkbox
            checked = props.value
            onChange = { e -> props.onValueChanged(e.target.checked) }
            ref = checkboxRef
        }

        label {
            css(classes("mt-4 parameter-label")) {
                display = Display.block
            }
            htmlFor = checkboxId
            +props.descriptor.id.label
        }
    }
}