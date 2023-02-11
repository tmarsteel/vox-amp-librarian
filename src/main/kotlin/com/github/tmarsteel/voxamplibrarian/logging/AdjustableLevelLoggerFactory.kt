package com.github.tmarsteel.voxamplibrarian.logging

import com.github.tmarsteel.voxamplibrarian.computeIfAbsent

object AdjustableLevelLoggerFactory : LoggerFactory {
    var rootLevel: Logger.Level = Logger.Level.INFO
    private val loggers = mutableMapOf<String, Logger>()

    override fun getLogger(module: String, handler: LogHandler): Logger {
        return loggers.computeIfAbsent(module) { LoggerImpl(module, Logger.Level.values().min(), handler) }
    }

    private class LoggerImpl(
        override val module: String,
        var level: Logger.Level,
        val handler: LogHandler,
    ) : Logger {
        override fun log(level: Logger.Level, message: String, vararg additionalObjects: Any) {
            if (level < this.level || level < rootLevel) {
                return
            }

            handler.handle(module, level, message, additionalObjects)
        }
    }
}