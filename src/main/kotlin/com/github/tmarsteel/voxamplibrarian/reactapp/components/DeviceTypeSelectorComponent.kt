package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceDescriptor
import react.FC
import react.Props
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select

external interface DeviceTypeSelectorComponentProps : Props {
    var types: List<DeviceDescriptor<*>>
    var value: DeviceDescriptor<*>
    var onChanged: (DeviceDescriptor<*>) -> Unit
}

val DeviceTypeSelectorComponent = FC<DeviceTypeSelectorComponentProps> { props ->
    select {
        multiple = false
        value = "${props.types.indexOf(props.value)}"
        onChange = { e ->
            props.onChanged(props.types[e.target.value.toInt()])
        }
        props.types.forEachIndexed { choiceIndex, choice ->
            option {
                value = "$choiceIndex"
                +choice.name
            }
        }
    }
}