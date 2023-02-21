package com.github.tmarsteel.voxamplibrarian.reactapp

import csstype.ClassName
import org.w3c.dom.HTMLElement
import react.ChildrenBuilder
import react.dom.html.HTMLAttributes
import react.dom.html.ReactHTML

fun classes(vararg names: String?): ClassName = names.filterNotNull().joinToString(separator = " ").unsafeCast<ClassName>()

fun ChildrenBuilder.icon(name: String, title: String, block: HTMLAttributes<HTMLElement>.() -> Unit = {}) {
    ReactHTML.i {
        this.title = title
        block()
        className = classes("bi bi-$name", this.className?.unsafeCast<String>())
    }
}