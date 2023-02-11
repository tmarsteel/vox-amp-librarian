package com.github.tmarsteel.voxamplibrarian.logging

interface Logger {
    val module: String

    fun log(level: Level, message: String, vararg additionalObjects: Any?)

    fun trace(message: String, vararg additionalObjects: Any?) = log(Level.TRACE, message, *additionalObjects)
    fun debug(message: String, vararg additionalObjects: Any?) = log(Level.DEBUG, message, *additionalObjects)
    fun info(message: String, vararg additionalObjects: Any?) = log(Level.INFO, message, *additionalObjects)
    fun warn(message: String, vararg additionalObjects: Any?) = log(Level.WARN, message, *additionalObjects)
    fun error(message: String, vararg additionalObjects: Any?) = log(Level.ERROR, message, *additionalObjects)

    enum class Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
}