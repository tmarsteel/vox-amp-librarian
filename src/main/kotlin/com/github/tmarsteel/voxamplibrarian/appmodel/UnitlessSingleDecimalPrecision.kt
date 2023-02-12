package com.github.tmarsteel.voxamplibrarian.appmodel

value class UnitlessSingleDecimalPrecision(val value: Int) : Comparable<UnitlessSingleDecimalPrecision> {
    override fun compareTo(other: UnitlessSingleDecimalPrecision) = this.value.compareTo(other.value)
}