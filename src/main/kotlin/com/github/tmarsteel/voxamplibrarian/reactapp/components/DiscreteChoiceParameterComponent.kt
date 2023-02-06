package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.DiscreteChoiceParameter
import csstype.Display
import csstype.TextAlign
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.span
import react.useState

external interface DiscreteChoiceParameterComponentProps : Props {
    var descriptor: DiscreteChoiceParameter<out Any>
    var value: Any
    var onValueChanged: (Any) -> Unit
}

val DiscreteChoiceParameterComponent = FC<DiscreteChoiceParameterComponentProps> { props ->
    val radioId by useState("bla-${props.descriptor.id.name}") // TODO: random
    props.descriptor.choices.forEachIndexed { choiceIndex, choice ->
        div {
            css {
                display = Display.block
            }
            input {
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
            }
        }
    }
    span {
        css {
            textAlign = TextAlign.center
            display = Display.inlineBlock
        }
    }
}