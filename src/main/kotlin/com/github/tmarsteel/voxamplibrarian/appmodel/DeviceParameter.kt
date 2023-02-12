package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.TubeBias
import kotlin.math.roundToInt
import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass as ProtocolAmpClass

sealed interface DeviceParameter<Value : Any> {
    val id: Id<Value>
    val default: Value

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
        object DelayModulationSpeed: Id<UnitlessSingleDecimalPrecision>()
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
        object ReverbTime : Id<UnitlessSingleDecimalPrecision>()
        object ReverbPreDelay : Id<Duration>()
        object ReverbLowDamp : Id<UnitlessSingleDecimalPrecision>()
        object ReverbHighDamp : Id<UnitlessSingleDecimalPrecision>()
    }
}

class ContinuousRangeParameter<V : Continuous<V>>(
    override val id: DeviceParameter.Id<V>,
    val valueRange: ClosedRange<V>,
    override val default: V,
    private val valueFactory: (Int) -> V,
) : DeviceParameter<V> {
    fun constructValue(intValue: Int): V = valueFactory(intValue)

    override fun rejectInvalidValue(value: V) {
        check(value in valueRange)
    }

    companion object {
        fun zeroToTenUnitless(id: DeviceParameter.Id<UnitlessSingleDecimalPrecision>, default: Double) : ContinuousRangeParameter<UnitlessSingleDecimalPrecision> {
            require(default in 0.0 .. 10.0) {
                "Default value not in range [0; 10]"
            }
            val defaultAsInt = (default * 10.0).roundToInt()
            require((defaultAsInt.toFloat() / 10.0 - default) in -0.00001 .. 0.00001) {
                "The default value specifies too much precision. Only a single decimal digit is supported."
            }

            return ContinuousRangeParameter(
                id,
                UnitlessSingleDecimalPrecision(0)..UnitlessSingleDecimalPrecision(100),
                UnitlessSingleDecimalPrecision(defaultAsInt),
                ::UnitlessSingleDecimalPrecision,
            )
        }
    }
}

class DiscreteChoiceParameter<Value : Any>(
    override val id: DeviceParameter.Id<Value>,
    val choices: Set<Value>,
    override val default: Value,
) : DeviceParameter<Value> {
    override fun rejectInvalidValue(value: Value) {
        check(value in choices)
    }

    companion object {
        inline operator fun <reified V : Enum<V>> invoke(
            id: DeviceParameter.Id<V>,
            default: V,
        ): DiscreteChoiceParameter<V> {
            return DiscreteChoiceParameter(id, enumValues<V>().toSet(), default)
        }
    }
}

class BooleanParameter(
    override val id: DeviceParameter.Id<Boolean>,
    override val default: Boolean,
) : DeviceParameter<Boolean> {
    override fun rejectInvalidValue(value: Boolean) {
        // nothing to do
    }
}