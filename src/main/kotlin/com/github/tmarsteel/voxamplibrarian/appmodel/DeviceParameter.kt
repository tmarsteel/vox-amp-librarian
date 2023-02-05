package com.github.tmarsteel.voxamplibrarian.appmodel

sealed interface DeviceParameter {
    val id: Id

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
        AMP_RESONANCE,
        AMP_TONE,
        AMP_TUBE_BIAS,
        AMP_CLASS,
        AMP_LOW_CUT,
        AMP_MID_BOOST,
        AMP_BRIGHT_CAP,
        PEDAL_LEVEL,
        PEDAL_MIX,
        COMP_SENSITIVITY,
        COMP_ATTACK,
        COMP_VOICE,
        CHORUS_SPEED,
        CHORUS_DEPTH,
        CHORUS_MANUAL,
        OVERDRIVE_DRIVE,
    }
}

class ContinuousUnitlessRangeParameter(
    override val id: DeviceParameter.Id,
    val valueRange: IntRange = 0..100,
    val semantic: Semantic = Semantic.ZERO_TO_TEN,
) : DeviceParameter {
    enum class Semantic {
        /** actual value from 0-100, displayed as 0.0 to 10.0 */
        ZERO_TO_TEN,

        /** actual value in millihertz */
        FREQUENCY,
    }
}

class DiscreteChoiceParameter<Value : Any>(
    override val id: DeviceParameter.Id,
    val choices: Array<Value>,
) : DeviceParameter {
    companion object {
        inline operator fun <reified V : Enum<V>> invoke(
            id: DeviceParameter.Id,
        ): DiscreteChoiceParameter<V> {
            return DiscreteChoiceParameter(id, enumValues())
        }
    }
}

class BooleanParameter(
    override val id: DeviceParameter.Id,
) : DeviceParameter