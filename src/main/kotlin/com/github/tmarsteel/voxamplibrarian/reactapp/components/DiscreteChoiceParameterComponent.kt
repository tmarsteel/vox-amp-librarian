package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.DiscreteChoiceParameter
import com.github.tmarsteel.voxamplibrarian.reactapp.IdGenerator
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.label
import csstype.Display
import csstype.None
import csstype.TextAlign
import csstype.pct
import emotion.react.css
import org.w3c.dom.HTMLLabelElement
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.span
import react.useState

external interface DiscreteChoiceParameterComponentProps : Props {
    var descriptor: DiscreteChoiceParameter<*>
    var value: Any
    var onValueChanged: (Any) -> Unit
}

val DiscreteChoiceParameterComponent = FC<DiscreteChoiceParameterComponentProps> { props ->
    val radioId by useState(IdGenerator.getUniqueId())
    div {
        className = classes("discrete-choices-container")
        props.descriptor.choices.forEachIndexed { choiceIndex, choice ->
            div {
                className = classes(
                    "discrete-choice",
                    "discrete-choice--checked".takeIf { choice == props.value },
                )
                onClick = {
                    val label = it.currentTarget.querySelector("label")!! as HTMLLabelElement
                    label.click()
                }

                input {
                    css {
                        display = None.none
                    }
                    type = InputType.radio
                    name = "discrete-choice-parameter-$radioId"
                    id = "discrete-choice-parameter-$radioId-$choiceIndex"
                    value = "$choiceIndex"
                    checked = choice == props.value
                    onChange = {
                        props.onValueChanged.unsafeCast<(Any) -> Unit>()(choice)
                    }
                }
                label {
                    htmlFor = "discrete-choice-parameter-$radioId-$choiceIndex"
                    +choice.toString()
                    onClick = { it.stopPropagation() }
                }
            }
        }
    }
    span {
        css(classes("parameter-label")) {
            textAlign = TextAlign.center
            display = Display.inlineBlock
            width = 100.pct
        }
        +props.descriptor.id.label
    }
}