package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.*
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3

external interface SimulationConfigurationComponentProps : Props {
    var configuration: SimulationConfiguration
    var onConfigurationChanged: (SimulationConfiguration) -> Unit
}

val SimulationConfigurationComponent = FC<SimulationConfigurationComponentProps> { props ->
    div {
        className = ClassName("row")
        props.configuration.programName?.let { programName ->
            div {
                className = ClassName("col-12")
                h2 {
                    +programName
                }
            }
        }
        div {
            className = ClassName("col-12")
            h3 {
                +"Amplifier"
            }
        }
    }
    (DeviceSlotComponent<AmplifierDescriptor>()) {
        deviceTypes = AmplifierDescriptor.ALL
        configuration = props.configuration.amplifier
        onConfigurationChanged = { newAmpConfig ->
            props.onConfigurationChanged(props.configuration.copy(amplifier = newAmpConfig))
        }
    }

    div {
        className = ClassName("row mt-4")
        div {
            className = ClassName("col-12")
        }
        h3 {
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

    div {
        className = ClassName("row mt-4")
        div {
            className = ClassName("col-12")
        }
        h3 {
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

    div {
        className = ClassName("row mt-4")
        div {
            className = ClassName("col-12")
        }
        h3 {
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