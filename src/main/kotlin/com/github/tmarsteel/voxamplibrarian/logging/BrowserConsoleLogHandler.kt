package com.github.tmarsteel.voxamplibrarian.logging

object BrowserConsoleLogHandler : LogHandler {
    override fun handle(module: String, level: Logger.Level, message: String, additionalObjects: Array<out Any?>) {
        when (level) {
            Logger.Level.TRACE,
            Logger.Level.DEBUG,
            Logger.Level.INFO -> console.info("[${level.name}]@$module $message", *additionalObjects)
            Logger.Level.WARN -> console.warn("[${level.name}]@$module $message", *additionalObjects)
            Logger.Level.ERROR -> console.error("[${level.name}]@$module $message", *additionalObjects)
        }
    }
}