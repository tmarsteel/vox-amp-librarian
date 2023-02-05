package com.github.tmarsteel.voxamplibrarian

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