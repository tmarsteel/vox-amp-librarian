package com.github.tmarsteel.voxamplibrarian.reactapp

import kotlinx.browser.window
import react.StateInstance
import react.useState
import kotlin.reflect.KProperty

class LocalStorageHook<T>(
    val key: String,
    val defaultValue: T,
    val cacheValue: Boolean,
    val serialize: (T) -> String,
    val deserialize: (String) -> T,
) {
    private var cacheInitialized: Boolean = false
    private var cachedValue: T = defaultValue

    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T {
        if (cacheValue && cacheInitialized) {
            return cachedValue
        }

        val value = window.localStorage.getItem(key)
            ?.let(deserialize)
            ?: defaultValue
        cachedValue = value
        cacheInitialized = true
        return value
    }

    operator fun setValue(thisRef: Nothing?, property: KProperty<*>, value: T) {
        if (cacheValue) {
            cachedValue = value
            cacheInitialized = true
        }

        window.localStorage.setItem(key, serialize(value))
    }

    companion object {
        fun <T> useLocalStorage(
            key: String,
            defaultValue: T,
            cacheValue: Boolean = true,
            serialize: (T) -> String = JSON::stringify,
            deserialize: (String) -> T = JSON::parse,
        ): LocalStorageHook<T> = LocalStorageHook(key, defaultValue, cacheValue, serialize, deserialize)
    }
}

class StateAndLocalStorageHook<T : Any> private constructor(
    private val stateInstance: StateInstance<T?>,
    private val localStorageHook: LocalStorageHook<T>,
) {
    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T {
        val fromState = stateInstance.getValue(thisRef, property)
        if (fromState != null) {
            return fromState
        }

        return localStorageHook.getValue(thisRef, property)
    }

    operator fun setValue(thisRef: Nothing?, property: KProperty<*>, value: T) {
        stateInstance.setValue(thisRef, property, value)
        localStorageHook.setValue(thisRef, property, value)
    }

    companion object {
        fun <T : Any> useStateBackedByLocalStorage(
            key: String,
            defaultValue: T,
            serialize: (T) -> String = JSON::stringify,
            deserialize: (String) -> T = JSON::parse
        ): StateAndLocalStorageHook<T> {
            return StateAndLocalStorageHook(
                useState(null),
                LocalStorageHook(key, defaultValue, true, serialize, deserialize),
            )
        }
    }
}