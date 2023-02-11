package com.github.tmarsteel.voxamplibrarian.logging

interface LoggerFactory {
    fun getLogger(module: String, handler: LogHandler): Logger

    companion object {
        var factoryImplentation: LoggerFactory = AdjustableLevelLoggerFactory
        var logHandler: LogHandler = BrowserConsoleLogHandler
        operator fun get(module: String) = factoryImplentation.getLogger(module, DelegatingLogHandler)
    }

    private object DelegatingLogHandler : LogHandler {
        override fun handle(module: String, level: Logger.Level, message: String, additionalObjects: Array<out Any>) = logHandler.handle(module, level, message, additionalObjects)
    }
}