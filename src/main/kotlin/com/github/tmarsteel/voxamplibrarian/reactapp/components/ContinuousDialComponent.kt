package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.ContinuousRangeParameter
import com.github.tmarsteel.voxamplibrarian.reactapp.parameterValue
import csstype.Display
import csstype.TextAlign
import csstype.VerticalAlign
import csstype.Width
import csstype.rem
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span

external interface ContinuousDialComponentProps : Props {
    var descriptor: ContinuousRangeParameter
    var value: Int
    var onValueChanged: ((Int) -> Unit)?
    var label: String?
}

val ContinuousDialComponent = FC<ContinuousDialComponentProps> { props ->
    div {
        input {
            css {
                display = Display.inlineBlock
                width = "calc(100% - 2.5rem)".unsafeCast<Width>()
            }
            type = InputType.range
            min = props.descriptor.valueRange.first.toDouble()
            max = props.descriptor.valueRange.last.toDouble()
            step = 1.0
            value = props.value.toString()
            onChange = { e ->
                props.onValueChanged?.invoke(e.target.valueAsNumber.toInt())
            }
        }
        div {
            css {
                textAlign = TextAlign.right
                display = Display.inlineBlock
                width = 2.5.rem
                verticalAlign = VerticalAlign.top
            }
            parameterValue(props.descriptor, props.value)
        }
        if (props.label != null) {
            span {
                css {
                    display = Display.block
                    textAlign = TextAlign.center
                }
                +props.label!!
            }
        }
    }
}