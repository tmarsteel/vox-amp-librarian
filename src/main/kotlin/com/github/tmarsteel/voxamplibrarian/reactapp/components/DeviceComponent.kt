package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import com.github.tmarsteel.voxamplibrarian.reactapp.ParameterOrder
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

external interface DeviceComponentProps : Props {
    var configuration: DeviceConfiguration<*>
    var onValueChanged: (DeviceParameter<*>, Any) -> Unit
}

val DeviceComponent = FC<DeviceComponentProps> { props ->
    div {
        className = ClassName("row")
        for (parameter in props.configuration.descriptor.parameters.sortedWith(ParameterOrder)) {
            div {
                className = ClassName("col-xs-12 col-sm-6 col-lg-4 col-xl-1 mt-4")
                ParameterComponent {
                    this.parameter = parameter
                    value = props.configuration.getValue(parameter.id)
                    onValueChanged = { newValue ->
                        props.onValueChanged(parameter, newValue)
                    }
                }
            }
        }
    }
}