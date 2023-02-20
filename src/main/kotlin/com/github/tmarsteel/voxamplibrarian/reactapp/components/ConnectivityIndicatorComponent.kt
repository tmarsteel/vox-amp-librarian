package com.github.tmarsteel.voxamplibrarian.reactapp.components

import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

external interface ConnectivityIndicatorComponentProps : Props {
    var isActive: Boolean
}

val ConnectivityIndicatorComponent = FC<ConnectivityIndicatorComponentProps> { props ->
    div {
        className = ClassName("connectivity-indicator connectivity-indicator-${if (props.isActive) "active" else "inactive"}")
    }
}