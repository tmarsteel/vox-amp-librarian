package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.*
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface SimulationConfigurationComponentProps : Props {
    var configuration: SimulationConfiguration
    var onConfigurationChanged: (SimulationConfiguration) -> Unit
}

val SimulationConfigurationComponent = FC<SimulationConfigurationComponentProps> { props ->
    ReactHTML.div {
        className = ClassName("row")
        ReactHTML.div {
            className = ClassName("col-12")
        }
        ReactHTML.h2 {
            +"Amplifier"
        }
    }
    (DeviceSlotComponent<AmplifierDescriptor>()) {
        deviceTypes = AmplifierDescriptor.ALL
        configuration = props.configuration.amplifier
        onConfigurationChanged = { newAmpConfig ->
            props.onConfigurationChanged(props.configuration.copy(amplifier = newAmpConfig))
        }
    }

    ReactHTML.div {
        className = ClassName("row mt-4")
        ReactHTML.div {
            className = ClassName("col-12")
        }
        ReactHTML.h2 {
            +"Pedal 1"
        }
    }
    (DeviceSlotComponent<SlotOnePedalDescriptor>()) {
        deviceTypes = SlotOnePedalDescriptor.ALL
        configuration = props.configuration.pedalOne
        onConfigurationChanged = { newPedalOneConfig ->
            props.onConfigurationChanged(props.configuration.copy(pedalOne = newPedalOneConfig))
        }
    }

    ReactHTML.div {
        className = ClassName("row mt-4")
        ReactHTML.div {
            className = ClassName("col-12")
        }
        ReactHTML.h2 {
            +"Pedal 2"
        }
    }
    (DeviceSlotComponent<SlotTwoPedalDescriptor>()) {
        deviceTypes = SlotTwoPedalDescriptor.ALL
        configuration = props.configuration.pedalTwo
        onConfigurationChanged = { newPedalTwoConfig ->
            props.onConfigurationChanged(props.configuration.copy(pedalTwo = newPedalTwoConfig))
        }
    }

    ReactHTML.div {
        className = ClassName("row mt-4")
        ReactHTML.div {
            className = ClassName("col-12")
        }
        ReactHTML.h2 {
            +"Reverb Pedal"
        }
    }
    (DeviceSlotComponent<ReverbPedalDescriptor>()) {
        deviceTypes = ReverbPedalDescriptor.ALL
        configuration = props.configuration.reverbPedal
        onConfigurationChanged = { newReverbPedalConfig ->
            props.onConfigurationChanged(props.configuration.copy(reverbPedal = newReverbPedalConfig))
        }
    }
}