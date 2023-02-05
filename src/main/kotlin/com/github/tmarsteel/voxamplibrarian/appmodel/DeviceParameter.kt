package com.github.tmarsteel.voxamplibrarian.appmodel

import kotlin.reflect.KClass

sealed interface DeviceParameter<Value : Any> {
    val id: Id
    val valueType: KClass<Value>

    fun rejectInvalidValue(value: Value)

    fun rejectInvalidValue(value: Any) {
        check(valueType.isInstance(value))
        @Suppress("UNCHECKED_CAST")
        rejectInvalidValue(value as Value)
    }

    /**
     * Each of these represents a single way that a user-configurable parameter
     * affects the final sound. This is under the assumption that no device has
     * two knobs that do the same thing; these IDs are assumed to unique in one device.
     */
    enum class Id {
        GAIN,
        EQ_BASS,
        EQ_MIDDLE,
        EQ_TREBLE,
        EQ_LOW_CUT,
        EQ_HIGH_CUT,
        EQ_TONE,
        AMP_VOLUME,
        AMP_PRESENCE,
        RESONANCE,
        AMP_TONE,
        AMP_TUBE_BIAS,
        AMP_CLASS,
        AMP_LOW_CUT,
        AMP_MID_BOOST,
        AMP_BRIGHT_CAP,
        AMP_NOISE_REDUCTION_SENSITIVITY,
        PEDAL_ENABLED,
        PEDAL_LEVEL,
        PEDAL_MIX,
        MODULATION_SPEED,
        MODULATION_DEPTH,
        MODULATION_MANUAL,
        COMP_SENSITIVITY,
        COMP_ATTACK,
        COMP_VOICE,
        OVERDRIVE_DRIVE,
        TREMOLO_DUTY,
        TREMOLO_SHAPE,
        DELAY_TIME,
        DELAY_FEEDBACK,
        REVERB_TIME,
        REVERB_PRE_DELAY,
        REVERB_LOW_DAMP,
        REVERB_HIGH_DAMP,
    }
}

class ContinuousRangeParameter(
    override val id: DeviceParameter.Id,
    val valueRange: IntRange = 0..100,
    val semantic: Semantic = Semantic.UNITLESS_SINGLE_DIGIT_PRECISION,
) : DeviceParameter<Int> {
    override val valueType = Int::class
    override fun rejectInvalidValue(value: Int) {
        check(value in valueRange)
    }

    enum class Semantic {
        /** value has no unit. The semantic value is one tenth of the actual value (soo 54 actual = 5.4 semantic) */
        UNITLESS_SINGLE_DIGIT_PRECISION,

        /** actual value in millihertz */
        FREQUENCY,

        /** actual value in milliseconds */
        TIME,
    }
}

class DiscreteChoiceParameter<Value : Any>(
    override val id: DeviceParameter.Id,
    override val valueType: KClass<Value>,
    val choices: Set<Value>,
) : DeviceParameter<Value> {
    override fun rejectInvalidValue(value: Value) {
        check(value in choices)
    }

    companion object {
        inline operator fun <reified V : Enum<V>> invoke(
            id: DeviceParameter.Id,
        ): DiscreteChoiceParameter<V> {
            return DiscreteChoiceParameter(id, V::class, enumValues<V>().toSet())
        }
    }
}

class BooleanParameter(
    override val id: DeviceParameter.Id,
) : DeviceParameter<Boolean> {
    override val valueType = Boolean::class
    override fun rejectInvalidValue(value: Boolean) {

    }
}