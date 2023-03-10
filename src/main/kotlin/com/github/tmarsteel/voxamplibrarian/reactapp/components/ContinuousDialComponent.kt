package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.*
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.label
import csstype.*
import emotion.react.css
import react.ChildrenBuilder
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import kotlin.math.absoluteValue

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
                width = 100.pct
                verticalAlign = VerticalAlign.top
            }
            renderContinuousValue(props.value)
        }
        span {
            css(classes("parameter-label")) {
                display = Display.block
                textAlign = TextAlign.center
                width = 100.pct
            }
            +props.descriptor.id.label
        }
    }
}

private val decimalSeparator: String = (1.5).asDynamic().toLocaleString().substring(1, 2)

private fun ChildrenBuilder.renderContinuousValue(value: Continuous<*>) {
    val text = when(value) {
        is UnitlessSingleDecimalPrecision -> (if (value.intValue < 0) "-" else "") + (value.intValue.absoluteValue / 10).asDynamic().toLocaleString() + decimalSeparator + (value.intValue.absoluteValue % 10).toString()
        is Duration -> "${value.milliseconds} ms"
        is Frequency -> (if (value.millihertz < 0) "-" else "") + (value.millihertz.absoluteValue / 1000).asDynamic().toLocaleString() + decimalSeparator + (value.millihertz.absoluteValue % 1000).toString().padStart(3, '0') + " Hz"
    }
    ReactHTML.span {
        +text
    }
}