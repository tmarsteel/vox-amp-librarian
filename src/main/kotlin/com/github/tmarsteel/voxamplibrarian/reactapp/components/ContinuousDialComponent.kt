package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.*
import csstype.*
import emotion.react.css
import react.ChildrenBuilder
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

external interface ContinuousDialComponentProps : Props {
    var descriptor: ContinuousRangeParameter<*>
    var value: Continuous<*>
    var onValueChanged: ((Continuous<*>) -> Unit)
}

val ContinuousDialComponent = FC<ContinuousDialComponentProps> { props ->
    div {
        className = ClassName("continuous-dial")
        RotarySliderComponent {
            range = props.descriptor.valueRange.start.intValue .. props.descriptor.valueRange.endInclusive.intValue
            value = props.value.intValue
            onChange = { newValueInt ->
                val newValue = props.descriptor.constructValue(newValueInt)
                props.onValueChanged(newValue)
            }
            size = 4.rem
        }
        div {
            css {
                textAlign = TextAlign.center
                display = Display.inlineBlock
                width = 4.rem
                verticalAlign = VerticalAlign.top
            }
            renderContinuousValue(props.value)
        }
        span {
            css {
                display = Display.block
                textAlign = TextAlign.center
            }
            +props.descriptor.id.name
        }
    }
}

private fun ChildrenBuilder.renderContinuousValue(value: Continuous<*>) {
    val text = when(value) {
        is UnitlessSingleDecimalPrecision -> (value.intValue / 10).toString() + "," + (value.intValue % 10).toString()
        is Duration -> "${value.milliseconds} ms"
        is Frequency -> (value.millihertz / 1000).toString() + "," + (value.millihertz % 1000).toString().padStart(3, '0') + " Hz"
    }
    ReactHTML.span {
        +text
    }
}