package com.github.tmarsteel.voxamplibrarian.reactapp.components

import csstype.Border
import csstype.ClassName
import csstype.Color
import csstype.Length
import csstype.LineStyle
import csstype.Margin
import csstype.Position
import csstype.Transform
import csstype.TransformOrigin
import csstype.Width
import csstype.pct
import csstype.px
import csstype.rem
import emotion.react.css
import kotlinx.js.jso
import react.Component
import react.Context
import react.FC
import react.Fragment
import react.Props
import react.RStatics
import react.ReactNode
import react.State
import react.create
import react.dom.html.ReactHTML.div

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

val RotarySliderComponent = FC<RotarySliderComponentProps> { props ->
    div {
        css {
            width = props.size
            height = props.size
        }
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
                    ROTATE_DEGREES_MIN + (props.value * (360 - CUTOUT_DEGREES) / (props.range.last - props.range.first))
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