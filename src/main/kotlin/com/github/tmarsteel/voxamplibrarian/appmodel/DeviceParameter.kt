package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.MutableProgram
import com.github.tmarsteel.voxamplibrarian.protocol.PedalSlot
import com.github.tmarsteel.voxamplibrarian.protocol.SingleByteProtocolSerializable
import com.github.tmarsteel.voxamplibrarian.protocol.TubeBias
import com.github.tmarsteel.voxamplibrarian.protocol.TwoByteDial
import com.github.tmarsteel.voxamplibrarian.protocol.ZeroToTenDial
import com.github.tmarsteel.voxamplibrarian.protocol.message.AmpDialTurnedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.EffectDialTurnedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToAmp
import com.github.tmarsteel.voxamplibrarian.protocol.message.NoiseReductionSensitivityChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.PedalActiveStateChangedMessage
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty1
import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass as ProtocolAmpClass

sealed interface DeviceParameter<Value : Any> {
    val id: Id<Value>
    val default: Value
    val protocolAdapter: ProtocolAdapter<Value>

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

    interface ProtocolAdapter<in Value : Any> {
        /**
         * Builds a message that sets this parameter to the given [value] on the amp.
         */
        fun buildUpdateMessage(value: Value): MessageToAmp<*>

        /**
         * Sets this parameter to the given [value] in the given [program].
         */
        fun applyToProgram(program: MutableProgram, value: Value)
    }
}

class ContinuousRangeParameter<V : Continuous<V>>(
    override val id: DeviceParameter.Id<V>,
    override val protocolAdapter: DeviceParameter.ProtocolAdapter<V>,
    val valueRange: ClosedRange<V>,
    override val default: V,
    private val valueFactory: (Int) -> V,
) : DeviceParameter<V> {
    fun constructValue(intValue: Int): V = valueFactory(intValue)

    override fun rejectInvalidValue(value: V) {
        check(value in valueRange)
    }

    companion object {
        fun zeroToTenUnitless(
            id: DeviceParameter.Id<UnitlessSingleDecimalPrecision>,
            protocolAdapter: DeviceParameter.ProtocolAdapter<UnitlessSingleDecimalPrecision>,
            default: Double
        ) : ContinuousRangeParameter<UnitlessSingleDecimalPrecision> {
            require(default in 0.0 .. 10.0) {
                "Default value not in range [0; 10]"
            }
            val defaultAsInt = (default * 10.0).roundToInt()
            require((defaultAsInt.toFloat() / 10.0 - default) in -0.00001 .. 0.00001) {
                "The default value specifies too much precision. Only a single decimal digit is supported."
            }

            return ContinuousRangeParameter(
                id,
                protocolAdapter,
                UnitlessSingleDecimalPrecision(0)..UnitlessSingleDecimalPrecision(100),
                UnitlessSingleDecimalPrecision(defaultAsInt),
                ::UnitlessSingleDecimalPrecision,
            )
        }
    }
}

class DiscreteChoiceParameter<Value : SingleByteProtocolSerializable>(
    override val id: DeviceParameter.Id<Value>,
    override val protocolAdapter: DeviceParameter.ProtocolAdapter<Value>,
    val choices: Set<Value>,
    override val default: Value,
) : DeviceParameter<Value> {
    override fun rejectInvalidValue(value: Value) {
        check(value in choices)
    }

    companion object {
        inline operator fun <reified V> invoke(
            id: DeviceParameter.Id<V>,
            protocolAdapter: DeviceParameter.ProtocolAdapter<V>,
            default: V,
        ): DiscreteChoiceParameter<V>
            where V : Enum<V>, V : SingleByteProtocolSerializable {
            return DiscreteChoiceParameter(id, protocolAdapter, enumValues<V>().toSet(), default)
        }
    }
}

class BooleanParameter(
    override val id: DeviceParameter.Id<Boolean>,
    override val protocolAdapter: DeviceParameter.ProtocolAdapter<Boolean>,
    override val default: Boolean,
) : DeviceParameter<Boolean> {
    override fun rejectInvalidValue(value: Boolean) {
        // nothing to do
    }
}

fun ampDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, in ZeroToTenDial>,
): DeviceParameter.ProtocolAdapter<Continuous<*>> {
    return object : DeviceParameter.ProtocolAdapter<Continuous<*>> {
        private fun Continuous<*>.asZeroToTen() = ZeroToTenDial(intValue.toByte())
        override fun buildUpdateMessage(value: Continuous<*>) = AmpDialTurnedMessage(index, value.asZeroToTen().asTwoByte())
        override fun applyToProgram(program: MutableProgram, value: Continuous<*>) {
            field.set(program, value.asZeroToTen())
        }
    }
}

fun ampSwitch(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, in Boolean>,
): DeviceParameter.ProtocolAdapter<Boolean> {
    return object : DeviceParameter.ProtocolAdapter<Boolean> {
        override fun buildUpdateMessage(value: Boolean) = AmpDialTurnedMessage(index, TwoByteDial(if (value) 0x01u else 0x00u))
        override fun applyToProgram(program: MutableProgram, value: Boolean) {
            field.set(program, value)
        }
    }
}

fun <V : SingleByteProtocolSerializable> ampDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, in V>,
): DeviceParameter.ProtocolAdapter<V> {
    return object : DeviceParameter.ProtocolAdapter<V> {
        override fun buildUpdateMessage(value: V) = AmpDialTurnedMessage(index, TwoByteDial(value.protocolValue.toUShort()))
        override fun applyToProgram(program: MutableProgram, value: V) {
            field.set(program, value)
        }
    }
}

object NoiseReductionSensitivityProtocolAdapter : DeviceParameter.ProtocolAdapter<UnitlessSingleDecimalPrecision> {
    override fun buildUpdateMessage(value: UnitlessSingleDecimalPrecision) = NoiseReductionSensitivityChangedMessage(
        ZeroToTenDial(value.intValue.toByte())
    )

    override fun applyToProgram(program: MutableProgram, value: UnitlessSingleDecimalPrecision) {
        program.noiseReductionSensitivity = ZeroToTenDial(value.intValue.toByte())
    }
}

fun PedalDescriptor.pedalEnabledSwitch() : DeviceParameter.ProtocolAdapter<Boolean> {
    return object : DeviceParameter.ProtocolAdapter<Boolean> {
        override fun buildUpdateMessage(value: Boolean) = PedalActiveStateChangedMessage(pedalType.slot, value)

        override fun applyToProgram(program: MutableProgram, value: Boolean) {
            pedalType.slot.programEnabledField.set(program, value)
        }
    }
}

fun PedalDescriptor.pedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, in ZeroToTenDial>,
) : DeviceParameter.ProtocolAdapter<Continuous<*>> = object : DeviceParameter.ProtocolAdapter<Continuous<*>> {
    private fun Continuous<*>.asZeroToTen() = ZeroToTenDial(intValue.toByte())
    override fun buildUpdateMessage(value: Continuous<*>) = EffectDialTurnedMessage(pedalType.slot, index, value.asZeroToTen().asTwoByte())

    override fun applyToProgram(program: MutableProgram, value: Continuous<*>) {
        field.set(program, value.asZeroToTen())
    }
}

fun PedalDescriptor.pedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, in TwoByteDial>,
) : DeviceParameter.ProtocolAdapter<Continuous<*>> = object : DeviceParameter.ProtocolAdapter<Continuous<*>> {
    private fun Continuous<*>.asTwoByteDial() = TwoByteDial(intValue.toUShort())
    override fun buildUpdateMessage(value: Continuous<*>) = EffectDialTurnedMessage(pedalType.slot, index, value.asTwoByteDial())

    override fun applyToProgram(program: MutableProgram, value: Continuous<*>) {
        field.set(program, value.asTwoByteDial())
    }
}

fun PedalDescriptor.pedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, in Byte>,
) : DeviceParameter.ProtocolAdapter<Continuous<*>> {
    return object : DeviceParameter.ProtocolAdapter<Continuous<*>> {
        override fun buildUpdateMessage(value: Continuous<*>) = EffectDialTurnedMessage(pedalType.slot, index, TwoByteDial(value.intValue.toUShort()))

        override fun applyToProgram(program: MutableProgram, value: Continuous<*>) {
            field.set(program, value.intValue.toByte())
        }
    }
}

fun <V : SingleByteProtocolSerializable> PedalDescriptor.pedalSelector(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, in Byte>,
) : DeviceParameter.ProtocolAdapter<V> {
    return object : DeviceParameter.ProtocolAdapter<V> {
        override fun buildUpdateMessage(value: V): MessageToAmp<*> {
            return EffectDialTurnedMessage(pedalType.slot, index, TwoByteDial(value.protocolValue.toUShort()))
        }

        override fun applyToProgram(program: MutableProgram, value: V) {
            field.set(program, value.protocolValue)
        }
    }
}

fun PedalDescriptor.pedalSwitch(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, in Byte>,
) : DeviceParameter.ProtocolAdapter<Boolean> {
    return object : DeviceParameter.ProtocolAdapter<Boolean> {
        override fun buildUpdateMessage(value: Boolean): MessageToAmp<*> {
            return EffectDialTurnedMessage(pedalType.slot, index, TwoByteDial(if (value) 0x01u else 0x00u))
        }

        override fun applyToProgram(program: MutableProgram, value: Boolean) {
            field.set(program, if (value) 0x01 else 0x00)
        }
    }
}