package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.reactapp.FullyConsumedWheelEventAssist.registerFullyConsumingWheelEventListener
import com.github.tmarsteel.voxamplibrarian.reactapp.GlobalMouseMoveAssist.registerGlobalDragHandler
import csstype.*
import emotion.react.css
import kotlinx.browser.window
import react.FC
import react.MutableRefObject
import react.Props
import react.dom.html.ReactHTML.div
import react.useRef
import kotlin.math.absoluteValue
import kotlin.math.sign

/** size of the area around the bottom center of the circle where the slider cannot move */
private const val CUTOUT_DEGREES: Int = 80

/** the amount of degrees to rotate from top-center to get to the min-point, with respect to the cutout */
private const val ROTATE_DEGREES_MIN: Int = -180 + (CUTOUT_DEGREES / 2)

/** the amount of degrees to rotate from top-center to get to the max-point, with respect to the cutout */
private const val ROTATE_DEGREES_MAX: Int = -ROTATE_DEGREES_MIN

/** size of the drag range of the knob in pixels (at 72dpi, density independent) */
private const val SENSITIVITY: Double = 399.0

external interface RotarySliderComponentProps : Props {
    var range: IntRange
    var value: Int
    var onChange: (Int) -> Unit
    var size: Length
}

private val logger = LoggerFactory["rotary-dial"]

val RotarySliderComponent = FC<RotarySliderComponentProps> { props ->
    val dragSensitivityFactor: Double = (props.range.last - props.range.first).toDouble() / SENSITIVITY / window.devicePixelRatio
    val currentDragStartScreenY: MutableRefObject<Int> = useRef(null)

    fun publishNewValue(delta: Int) {
        if (delta == 0) {
            return
        }

        val newValue = (props.value + delta).coerceIn(props.range)
        props.onChange(newValue)
    }

    div {
        css(ClassName("rotary-slider")) {
            width = props.size
            height = props.size
            touchAction = None.none
        }

        tabIndex = 0
        registerGlobalDragHandler(
            onDragStart = {
                currentDragStartScreenY.current = it.screenY
            },
            onDrag = { event ->
                currentDragStartScreenY.current?.let { startY ->
                    val delta = ((startY - event.screenY).toDouble() * dragSensitivityFactor).toInt()
                    publishNewValue(delta)
                }
            },
            onDragEnd = {
                currentDragStartScreenY.current = null
            },
        )
        onKeyDown = keydown@ { event ->
            if (window.document.activeElement !== event.target) {
                return@keydown
            }

            var delta = 0
            when (event.code) {
                "ArrowUp", "ArrowRight" -> {
                    delta++
                    event.preventDefault()
                }
                "ArrowDown", "ArrowLeft" -> {
                    delta--
                    event.preventDefault()
                }
            }
            publishNewValue(delta)
        }
        registerFullyConsumingWheelEventListener { event ->
            val delta = (-event.deltaY.sign * (props.range.last - props.range.first).toDouble() / 25.0).toInt()
                .coerceAbsoluteAtLeast(1)
            event.stopPropagation()

            publishNewValue(delta)
        }
        div {
            css(ClassName("rotary-slider__marker-container")) {
                transform = "rotate(${ROTATE_DEGREES_MIN}deg)".unsafeCast<Transform>()
            }
            div {
                className = ClassName("rotary-slider__marker rotary-slider__marker--min")
            }
        }
        div {
            css(ClassName("rotary-slider__marker-container")) {
                marginTop = (-100).pct
                transform = "rotate(${ROTATE_DEGREES_MAX}deg)".unsafeCast<Transform>()
            }
            div {
                className = ClassName("rotary-slider__marker rotary-slider__marker--max")
            }
        }
        div {
            css(ClassName("rotary-slider__knob")) {
                val nDegrees = ROTATE_DEGREES_MIN + ((props.value - props.range.first) * (360 - CUTOUT_DEGREES) / (props.range.last - props.range.first))
                transform = "rotate(${nDegrees}deg)".unsafeCast<Transform>()
            }

            div {
                className = ClassName("rotary-slider__marker")
            }
        }
    }
}

private fun Int.coerceAbsoluteAtLeast(minAbsValue: Int): Int {
    val actualAbs = this.absoluteValue
    if (actualAbs >= minAbsValue) {
        return this
    }

    return sign * minAbsValue
}
