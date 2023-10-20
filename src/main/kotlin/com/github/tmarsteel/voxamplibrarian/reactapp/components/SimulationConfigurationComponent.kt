package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.*
import com.github.tmarsteel.voxamplibrarian.reactapp.icon
import csstype.ClassName
import csstype.Cursor
import csstype.pct
import csstype.rem
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.input
import react.useState

external interface SimulationConfigurationComponentProps : Props {
    var configuration: SimulationConfiguration
    var onConfigurationChanged: (SimulationConfiguration) -> Unit
}

val SimulationConfigurationComponent = FC<SimulationConfigurationComponentProps> { props ->
    var editName: Boolean by useState(false)
    var programNameInEdit: String? by useState(null)
    div {
        className = ClassName("row")
        props.configuration.programName?.let { programName ->
            div {
                className = ClassName("col-12")
                if (editName) {
                    input {
                        css {
                            fontSize = 200.pct
                            height = 2.5.rem
                            marginBottom = 0.5.rem
                        }
                        value = programNameInEdit ?: ""
                        onChange = {
                            programNameInEdit = it.target.value
                        }
                        maxLength = 16
                    }
                    button {
                        icon("check-circle-fill", "apply") {
                            css {
                                fontSize = 1.5.rem
                            }
                            onClick = {
                                editName = false
                                props.onConfigurationChanged(
                                    props.configuration.copy(
                                        programName = programNameInEdit ?: ""
                                    )
                                )
                            }
                        }
                    }
                } else {
                    h2 {
                        css {
                            cursor = Cursor.text
                            height = 2.5.rem
                        }
                        +programName
                        +" "
                        button {
                            icon("pencil-fill", "Edit name")
                        }
                        onClick = {
                            editName = true
                            programNameInEdit = props.configuration.programName ?: ""
                        }
                    }
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