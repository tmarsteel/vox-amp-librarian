package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.BooleanParameter
import csstype.Auto
import csstype.AutoLength
import csstype.ClassName
import csstype.Display
import csstype.Margin
import csstype.TextAlign
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import csstype.rem
import react.dom.html.ReactHTML.span

external interface BooleanParameterComponentProps : Props {
    var descriptor: BooleanParameter
    var value: Boolean
    var onValueChanged: (Boolean) -> Unit
}

val BooleanParameterComponent = FC<BooleanParameterComponentProps> { props ->
    div {
        css {
            width = Auto.auto
            textAlign = TextAlign.center
        }

        input {
            css {
                width = 1.5.rem
                height = 1.5.rem
            }
            type = InputType.checkbox
            checked = props.value
            onChange = { e -> props.onValueChanged(e.target.checked) }
        }
        span {
            css {
                display = Display.block
            }
            +props.descriptor.id.name
        }
    }
}