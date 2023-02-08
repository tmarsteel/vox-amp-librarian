package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface DeviceSlotComponentProps<T : DeviceDescriptor> : Props {
    var deviceTypes: List<T>
    var configuration: DeviceConfiguration<T>
    var onConfigurationChanged: (DeviceConfiguration<T>) -> Unit
}

private val DeviceSlotComponentImpl = FC<DeviceSlotComponentProps<*>> { props ->
    ReactHTML.div {
        className = ClassName("row")
        ReactHTML.div {
            className = ClassName("col-12")
            DeviceTypeSelectorComponent {
                types = props.deviceTypes
                value = props.configuration.descriptor
                onChanged = { newType ->
                    // TODO: carry as many settings to the new device
                    props.onConfigurationChanged.unsafeCast<(DeviceConfiguration<*>) -> Unit>()(DeviceConfiguration(newType, newType.defaults))
                }
            }
        }
    }
    DeviceComponent {
        configuration = props.configuration
        onValueChanged = { param, newValue ->
            props.onConfigurationChanged.unsafeCast<(DeviceConfiguration<*>) -> Unit>()(
                props.configuration.withValue(param.unsafeCast<DeviceParameter<Any>>(), newValue)
            )
        }
    }
}
fun <T : DeviceDescriptor> DeviceSlotComponent(): FC<DeviceSlotComponentProps<T>> = DeviceSlotComponentImpl.unsafeCast<FC<DeviceSlotComponentProps<T>>>()

private fun <D : DeviceDescriptor, T : Any> DeviceConfiguration<D>.withValue(parameter: DeviceParameter<T>, newValue: T): DeviceConfiguration<D> {
    return copy(
        values = values.toMutableMap()
            .also {
                it[parameter.id] = newValue
            }
    )
}