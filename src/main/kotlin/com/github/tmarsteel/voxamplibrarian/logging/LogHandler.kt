package com.github.tmarsteel.voxamplibrarian.logging

interface LogHandler {
    fun handle(module: String, level: Logger.Level, message: String, additionalObjects: Array<out Any?>)
}