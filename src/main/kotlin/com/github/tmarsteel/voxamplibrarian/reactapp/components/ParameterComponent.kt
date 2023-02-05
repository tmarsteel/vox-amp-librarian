package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.BooleanParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.ContinuousRangeParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import react.FC
import react.Props
import react.dom.html.ReactHTML.span

external interface ParameterComponentProps : Props {
    var parameter: DeviceParameter<*>
    var value: Any
    var onValueChanged: (Any) -> Unit
}

val ParameterComponent = FC<ParameterComponentProps> { props ->
    when(val parameter = props.parameter) {
        is ContinuousRangeParameter -> {
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
        else -> {
            span {
                +"unsupported parameter of type ${parameter::class.simpleName}"
            }
        }
    }
}