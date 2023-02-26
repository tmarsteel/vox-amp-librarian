package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface DeviceSlotComponentProps<T : DeviceDescriptor<*>> : Props {
    var deviceTypes: List<T>
    var configuration: DeviceConfiguration<T>
    var onConfigurationChanged: (DeviceConfiguration<T>) -> Unit
}

private val DeviceSlotComponentImpl = FC<DeviceSlotComponentProps<*>> { props ->
    ReactHTML.div {
        className = ClassName("row")
        ReactHTML.div {
            className = ClassName("col-12 mb-3")
            DeviceTypeSelectorComponent {
                types = props.deviceTypes
                value = props.configuration.descriptor
                onChanged = { newType ->
                    props.onConfigurationChanged.unsafeCast<(DeviceConfiguration<*>) -> Unit>()(props.configuration.withDescriptor(newType))
                }
            }
        }
    }
    DeviceComponent {
        configuration = props.configuration
        onValueChanged = { param, newValue ->
            props.onConfigurationChanged.unsafeCast<(DeviceConfiguration<*>) -> Unit>()(
                props.configuration.withValue(param.id.unsafeCast<DeviceParameter.Id<Any>>(), newValue)
            )
        }
    }
}
fun <T : DeviceDescriptor<*>> DeviceSlotComponent(): FC<DeviceSlotComponentProps<T>> = DeviceSlotComponentImpl.unsafeCast<FC<DeviceSlotComponentProps<T>>>()