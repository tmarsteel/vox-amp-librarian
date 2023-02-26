package com.github.tmarsteel.voxamplibrarian.reactapp

object IdGenerator {
    private var counter: Int = 0
    fun getUniqueId(): String = "${counter++}"
}