package com.github.tmarsteel.voxamplibrarian.appmodel

sealed interface Continuous<Self : Continuous<Self>> : Comparable<Self> {
    val intValue: Int
    override fun compareTo(other: Self): Int = this.intValue.compareTo(other.intValue)
}

data class Frequency(val millihertz: Int) : Continuous<Frequency> {
    override val intValue get() = millihertz

    override fun toString() = "$millihertz mHz"

    companion object {
        val Int.mHz: Frequency get() = Frequency(this)
    }
}

data class UnitlessSingleDecimalPrecision(override val intValue: Int) : Continuous<UnitlessSingleDecimalPrecision> {
    override fun toString() = intValue.toString()
}

data class Duration(val milliseconds: Int) : Continuous<Duration> {
    override val intValue get() = milliseconds

    override fun toString() = "$milliseconds ms"

    companion object {
        val Int.ms: Duration get() = Duration(this)
    }
}