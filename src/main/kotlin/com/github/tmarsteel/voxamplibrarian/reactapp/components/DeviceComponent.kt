package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

external interface DeviceComponentProps : Props {
    var configuration: DeviceConfiguration<*>
    var onValueChanged: (DeviceParameter<*>, Any) -> Unit
}

val DeviceComponent = FC<DeviceComponentProps> { props ->
    div {
        className = ClassName("row")
        div {
            className = ClassName("col-12")
            span {
                +props.configuration.descriptor.name
            }
        }
        for (parameter in props.configuration.descriptor.parameters) {
            div {
                className = ClassName("col-2 mt-4")
                ParameterComponent {
                    this.parameter = parameter
                    value = props.configuration.values.getValue(parameter.id)
                    onValueChanged = { newValue ->
                        props.onValueChanged(parameter, newValue)
                    }
                }
            }
        }
    }
}