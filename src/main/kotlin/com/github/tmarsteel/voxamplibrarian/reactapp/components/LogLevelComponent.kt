package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.logging.AdjustableLevelLoggerFactory
import com.github.tmarsteel.voxamplibrarian.logging.Logger
import com.github.tmarsteel.voxamplibrarian.reactapp.LocalStorageHook.Companion.useLocalStorage
import csstype.rem
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.useEffectOnce

val LogLevelComponent = FC<Props> {
    var level by useLocalStorage("log-level", defaultValue = AdjustableLevelLoggerFactory.rootLevel, serialize = Logger.Level::name, deserialize = Logger.Level::valueOf)
    useEffectOnce {
        AdjustableLevelLoggerFactory.rootLevel = level
    }
    label {
        css {
            marginRight = 1.rem
        }
        +"Log-Level"
        htmlFor = "log-level-select"
    }
    select {
        id = "log-level-select"
        Logger.Level.values().forEach { level ->
            option {
                value = level.name
                +level.name
            }
        }
        defaultValue = level.name
        onChange = { selectElement ->
            val newLevel = Logger.Level.valueOf(selectElement.target.value)
            AdjustableLevelLoggerFactory.rootLevel = newLevel
            level = newLevel
        }
    }
}