package com.github.tmarsteel.voxamplibrarian

import kotlinx.browser.window
import org.khronos.webgl.Uint8Array

fun <K : Any, V> MutableMap<K, V>.computeIfAbsent(key: K, compute: (K) -> V): V {
    this[key]?.let { return it }
    val value = compute(key)
    this[key] = value
    return value
}

fun <K, V> MutableMap<K, V>.putIfAbsent(key: K, value: V): Boolean {
    if (key in this) {
        return false
    }

    this[key] = value
    return true
}

fun installPolyfills() {
    val window = window.asDynamic()
    if (window.crypto == undefined) {
        window.crypto = Any()
    }

    if (window.crypto.randomUUID == undefined) {
        window.crypto.randomUUID = ({
            val randomBytes = Uint8Array(16).asDynamic()
            window.crypto.getRandomValues(randomBytes)
            // taken from java.util.UUID.randomUUID()
            randomBytes[6] = (randomBytes[6] as Int) and 0x0f;  /* clear version        */
            randomBytes[6] = (randomBytes[6] as Int) or 0x40;   /* set to version 4     */
            randomBytes[8] = (randomBytes[8] as Int) and 0x3f;  /* clear variant        */
            randomBytes[8] = (randomBytes[8] as Int) or 0x80;   /* set to IETF variant  */

            val resultBuilder = StringBuilder()
            var i = 0
            fun appendByte() {
                resultBuilder.append((randomBytes[i++] as Number).toByte().hex())
            }
            repeat(4) { appendByte() }
            resultBuilder.append('-')
            repeat(2) { appendByte() }
            resultBuilder.append('-')
            repeat(2) { appendByte() }
            resultBuilder.append('-')
            repeat(2) { appendByte() }
            resultBuilder.append('-')
            repeat(6) { appendByte() }
        }) as () -> String
    }
}