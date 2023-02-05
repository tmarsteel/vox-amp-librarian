package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.ContinuousRangeParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import com.github.tmarsteel.voxamplibrarian.reactapp.components.ContinuousDialComponent
import csstype.ClassName
import kotlinx.browser.document
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.useState

val AppComponent = FC<Props> {
    var paramValue: Int by useState(0)
    div {
        className = ClassName("container")
        div {
            className = ClassName("row")
            div {
                className = ClassName("col-2")
                ContinuousDialComponent {
                    descriptor = ContinuousRangeParameter(DeviceParameter.Id.RESONANCE)
                    value = paramValue
                    onValueChanged = { paramValue = it }
                    label = "Gain"
                }
            }
        }
    }
}

fun main() {
    val rootElement = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(rootElement).render(Fragment.create {
        AppComponent {

        }
    })
}
