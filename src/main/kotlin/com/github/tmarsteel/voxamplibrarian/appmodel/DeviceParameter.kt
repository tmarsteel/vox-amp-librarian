package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.TubeBias
import kotlin.time.Duration
import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass as ProtocolAmpClass

sealed interface DeviceParameter<Value : Any> {
    val id: Id<Value>

    fun rejectInvalidValue(value: Value)

    /**
     * Each of these represents a single way that a user-configurable parameter
     * affects the final sound. This is under the assumption that no device has
     * two knobs that do the same thing; these IDs are assumed to unique in one device.
     */
    sealed class Id<Value : Any> {
        val name: String get() = this::class.simpleName!!
        override fun toString() = name

        object Gain : Id<UnitlessSingleDecimalPrecision>()
        object EqBass : Id<UnitlessSingleDecimalPrecision>()
        object EqMiddle : Id<UnitlessSingleDecimalPrecision>()
        object EqTreble : Id<UnitlessSingleDecimalPrecision>()
        object EqLowCut : Id<Boolean>()
        object EqHighCut : Id<Boolean>()
        object EqTone : Id<UnitlessSingleDecimalPrecision>()
        object AmpVolume : Id<UnitlessSingleDecimalPrecision>()
        object AmpPresence : Id<UnitlessSingleDecimalPrecision>()
        object Resonance : Id<UnitlessSingleDecimalPrecision>()
        object AmpTone : Id<UnitlessSingleDecimalPrecision>()
        object AmpTubeBias : Id<TubeBias>()
        object AmpClass : Id<ProtocolAmpClass>()
        object AmpLowCut : Id<Boolean>()
        object AmpMidBoost : Id<Boolean>()
        object AmpBrightCap : Id<Boolean>()
        object AmpNoiseReductionSensitivity : Id<UnitlessSingleDecimalPrecision>()
        object PedalEnabled : Id<Boolean>()
        object PedalLevel : Id<UnitlessSingleDecimalPrecision>()
        object PedalMix : Id<UnitlessSingleDecimalPrecision>()
        object ModulationSpeed : Id<Frequency>()
        object ModulationDepth : Id<UnitlessSingleDecimalPrecision>()
        object ModulationManual : Id<UnitlessSingleDecimalPrecision>()
        object CompSensitivity : Id<UnitlessSingleDecimalPrecision>()
        object CompAttack : Id<UnitlessSingleDecimalPrecision>()
        object CompVoice : Id<CompressorPedalDescriptor.Voice>()
        object OverdriveDrive : Id<UnitlessSingleDecimalPrecision>()
        object TremoloDuty : Id<UnitlessSingleDecimalPrecision>()
        object TremoloShape : Id<UnitlessSingleDecimalPrecision>()
        object DelayTime : Id<Duration>()
        object DelayFeedback : Id<UnitlessSingleDecimalPrecision>()
        object ReverbTime : Id<Duration>()
        object ReverbPreDelay : Id<UnitlessSingleDecimalPrecision>()
        object ReverbLowDamp : Id<UnitlessSingleDecimalPrecision>()
        object ReverbHighDamp : Id<UnitlessSingleDecimalPrecision>()
    }
}

class ContinuousRangeParameter<V : Comparable<V>>(
    override val id: DeviceParameter.Id<V>,
    val valueRange: ClosedRange<V>,
) : DeviceParameter<V> {
    override fun rejectInvalidValue(value: V) {
        check(value in valueRange)
    }

    companion object {
        fun zeroToTenUnitless(id: DeviceParameter.Id<UnitlessSingleDecimalPrecision>) = ContinuousRangeParameter(
            id,
            UnitlessSingleDecimalPrecision(0)..UnitlessSingleDecimalPrecision(1)
        )
    }
}

class DiscreteChoiceParameter<Value : Any>(
    override val id: DeviceParameter.Id<Value>,
    val choices: Set<Value>,
) : DeviceParameter<Value> {
    override fun rejectInvalidValue(value: Value) {
        check(value in choices)
    }

    companion object {
        inline operator fun <reified V : Enum<V>> invoke(
            id: DeviceParameter.Id<V>,
        ): DiscreteChoiceParameter<V> {
            return DiscreteChoiceParameter(id, enumValues<V>().toSet())
        }
    }
}

class BooleanParameter(
    override val id: DeviceParameter.Id<Boolean>,
) : DeviceParameter<Boolean> {
    override fun rejectInvalidValue(value: Boolean) {
        // nothing to do
    }
}