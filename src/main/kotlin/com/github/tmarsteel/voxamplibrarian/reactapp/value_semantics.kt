package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.ContinuousRangeParameter
import react.ChildrenBuilder
import react.dom.html.ReactHTML.span

fun ChildrenBuilder.parameterValue(descriptor: ContinuousRangeParameter, value: Int) {
    val text = when(descriptor.semantic) {
        ContinuousRangeParameter.Semantic.UNITLESS_SINGLE_DIGIT_PRECISION -> (value / 10).toString() + "," + (value % 10).toString()
        ContinuousRangeParameter.Semantic.TIME -> "$value ms"
        ContinuousRangeParameter.Semantic.FREQUENCY -> (value / 1000).toString() + "," + (value % 1000).toString().padStart(3, '0') + " Hz"
    }
    span {
        +text
    }
}