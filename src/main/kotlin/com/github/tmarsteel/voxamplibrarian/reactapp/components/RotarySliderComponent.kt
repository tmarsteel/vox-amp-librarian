package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.reactapp.GlobalMouseMoveAssist.registerGlobalDragHandler
import csstype.Border
import csstype.ClassName
import csstype.Color
import csstype.Length
import csstype.LineStyle
import csstype.Margin
import csstype.Transform
import csstype.Width
import csstype.pct
import csstype.rem
import emotion.react.css
import org.w3c.dom.events.MouseEvent
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useState

/** size of the area around the bottom center of the circle where the slider cannot move */
private const val CUTOUT_DEGREES: Int = 80

/** the amount of degrees to rotate from top-center to get to the min-point, with respect to the cutout */
private const val ROTATE_DEGREES_MIN: Int = -180 + (CUTOUT_DEGREES / 2)

/** the amount of degrees to rotate from top-center to get to the max-point, with respect to the cutout */
private const val ROTATE_DEGREES_MAX: Int = -ROTATE_DEGREES_MIN

external interface RotarySliderComponentProps : Props {
    var range: IntRange
    var value: Int
    var onChange: (Int) -> Unit
    var size: Length
}

private sealed class Modification {
    abstract val pendingChange: Int

    object Inactive : Modification() {
        override val pendingChange = 0
    }
    class Dragging(downEvent: MouseEvent) : Modification() {
        private var startScreenY: Int = downEvent.screenY
        private var currentScreenY: Int = downEvent.screenY
        override val pendingChange: Int get() = currentScreenY - startScreenY

        fun onMouseMoved(event: MouseEvent) {
            currentScreenY = event.screenY
            console.log("move", startScreenY, currentScreenY)
        }
    }
}

val RotarySliderComponent = FC<RotarySliderComponentProps> { props ->
    var mode: Modification by useState(Modification.Inactive)
    var displayValue by useState(props.value)

    div {
        css {
            width = props.size
            height = props.size
        }
        registerGlobalDragHandler(
            onDragStart = {
                mode = Modification.Dragging(it)
            },
            onDrag = { event ->
                console.log(event, mode)
                (mode as? Modification.Dragging)?.onMouseMoved(event)
                console.log(mode)
                displayValue = props.value + mode.pendingChange
            },
            onDragEnd = {
                val finalValue = props.value + mode.pendingChange
                displayValue = finalValue
                props.onChange(finalValue)
                mode = Modification.Inactive
            },
        )
        div {
            css {
                width = 100.pct
                height = 100.pct
                transform = "rotate(${ROTATE_DEGREES_MIN}deg)".unsafeCast<Transform>()
            }
            div {
                css(ClassName("rotary-slider__marker rotary-slider__marker--min")) {
                    width = "calc(50% - 0.05rem)".unsafeCast<Width>()
                    height = 10.pct
                    borderRight = Border(width = 0.1.rem, style = LineStyle.solid, color = Color("black"))
                }
            }
        }
        div {
            css {
                marginTop = (-100).pct
                width = 100.pct
                height = 100.pct
                transform = "rotate(${ROTATE_DEGREES_MAX}deg)".unsafeCast<Transform>()
            }
            div {
                css(ClassName("rotary-slider__marker rotary-slider__marker--max")) {
                    width = "calc(50% - 0.05rem)".unsafeCast<Width>()
                    height = 10.pct
                    borderRight = Border(width = 0.1.rem, style = LineStyle.solid, color = Color("black"))
                }
            }
        }
        div {
            css {
                width = 80.pct
                height = 80.pct
                margin = Margin(top = (-90).pct, left = 10.pct, right = 10.pct, bottom = 10.pct)
                borderColor = Color("black")
                borderStyle = LineStyle.solid
                borderWidth = 0.1.rem
                borderRadius = 50.pct
                val x =
                    ROTATE_DEGREES_MIN + (displayValue * (360 - CUTOUT_DEGREES) / (props.range.last - props.range.first))
                transform = "rotate(${x}deg)".unsafeCast<Transform>()
            }

            div {
                css {
                    width = "calc(50% - 0.05rem)".unsafeCast<Width>()
                    height = 11.11.pct
                    borderRight = Border(width = 0.1.rem, style = LineStyle.solid, color = Color("black"))
                }
            }
        }
    }
}
