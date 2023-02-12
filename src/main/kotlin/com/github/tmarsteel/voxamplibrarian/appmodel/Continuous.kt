package com.github.tmarsteel.voxamplibrarian.appmodel

sealed interface Continuous<Self : Continuous<Self>> : Comparable<Self> {
    val intValue: Int
    override fun compareTo(other: Self): Int = this.intValue.compareTo(other.intValue)
}

class Frequency(val millihertz: Int) : Continuous<Frequency> {
    override val intValue get() = millihertz

    companion object {
        val Int.mHz: Frequency get() = Frequency(this)
    }
}

class UnitlessSingleDecimalPrecision(override val intValue: Int) : Continuous<UnitlessSingleDecimalPrecision>

class Duration(val milliseconds: Int) : Continuous<Duration> {
    override val intValue get() = milliseconds

    companion object {
        val Int.ms: Duration get() = Duration(this)
    }
}