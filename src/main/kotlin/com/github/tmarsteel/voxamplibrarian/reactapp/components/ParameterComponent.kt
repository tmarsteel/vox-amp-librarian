package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.BooleanParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.ContinuousRangeParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.DiscreteChoiceParameter
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import react.FC
import react.Props

external interface ParameterComponentProps : Props {
    var parameter: DeviceParameter<*>
    var value: Any
    var onValueChanged: (Any) -> Unit
}

private val logger = LoggerFactory["react:parameter-component"]

val ParameterComponent = FC<ParameterComponentProps> { props ->
    when(val parameter = props.parameter) {
        is ContinuousRangeParameter -> {
            if (props.value !is Int) {
                logger.error("Value should be an int", props.value)
            }
            ContinuousDialComponent {
                descriptor = parameter
                value = props.value as Int
                onValueChanged = props.onValueChanged
            }
        }
        is BooleanParameter -> {
            BooleanParameterComponent {
                descriptor = parameter
                value = props.value as Boolean
                onValueChanged = props.onValueChanged
            }
        }
        is DiscreteChoiceParameter<*> -> {
            DiscreteChoiceParameterComponent {
                descriptor = parameter
                value = props.value
                onValueChanged = props.onValueChanged
            }
        }
    }
}