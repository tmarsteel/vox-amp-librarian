package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.appmodel.DiscreteChoiceParameter
import com.github.tmarsteel.voxamplibrarian.reactapp.label
import csstype.Display
import csstype.TextAlign
import csstype.pct
import csstype.rem
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
    var descriptor: DiscreteChoiceParameter<*>
    var value: Any
    var onValueChanged: (Any) -> Unit
}

private object IdGenerator {
    private var counter: Int = 0
    fun getUniqueId(): String = "${counter++}"
}

val DiscreteChoiceParameterComponent = FC<DiscreteChoiceParameterComponentProps> { props ->
    val radioId by useState(IdGenerator.getUniqueId())
    props.descriptor.choices.forEachIndexed { choiceIndex, choice ->
        div {
            css {
                display = Display.block
                textAlign = TextAlign.center
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
                css {
                    marginLeft = 0.5.rem
                }
                htmlFor = "discrete-choice-parameter-$radioId-$choiceIndex"
                +choice.toString()
            }
        }
    }
    span {
        css {
            textAlign = TextAlign.center
            display = Display.inlineBlock
            width = 100.pct
        }
        +props.descriptor.id.label
    }
}