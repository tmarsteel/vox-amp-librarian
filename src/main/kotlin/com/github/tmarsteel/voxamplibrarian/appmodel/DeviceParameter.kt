package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.hex
import com.github.tmarsteel.voxamplibrarian.protocol.*
import com.github.tmarsteel.voxamplibrarian.protocol.message.*
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty1
import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass as ProtocolAmpClass

sealed interface DeviceParameter<Value : Any> {
    val id: Id<Value>
    val default: Value

    /**
     * @return a valid value for this parameter that is as close to the given
     * [value] as possible.
     */
    fun coerceValid(value: Value): Value

    /**
     * Builds a message that sets this parameter to the given [value] on the amp.
     */
    fun buildUpdateMessage(value: Value): MessageToAmp<*>

    /**
     * Sets this parameter to the given [value] in the given [program].
     */
    fun applyToProgram(program: MutableProgram, value: Value)

    fun getValueFrom(program: Program): Value

    /**
     * @return the new value of this parameter after it was affected by the given [event],
     * or `null` if unaffected.
     */
    fun tryGetNewValueFromEvent(event: MessageToHost): Value?

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

    interface ProtocolAdapter<in Parameter : DeviceParameter<Value>, Value : Any> {
        /**
         * Builds a message that sets this parameter to the given [value] on the amp.
         */
        fun buildUpdateMessage(parameter: Parameter, value: Value): MessageToAmp<*>

        /**
         * Sets this parameter to the given [value] in the given [program].
         */
        fun applyToProgram(program: MutableProgram, parameter: Parameter, value: Value)

        fun getValueFrom(program: Program, parameter: Parameter): Value

        /**
         * @return the new value of this parameter after it was affected by the given [event],
         * or `null` if unaffected.
         */
        fun tryGetNewValueFromEvent(parameter: Parameter, event: MessageToHost): Value?
    }
}

class ContinuousRangeParameter<V : Continuous<V>>(
    override val id: DeviceParameter.Id<V>,
    private val protocolAdapter: DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<V>, V>,
    val valueRange: ClosedRange<V>,
    override val default: V,
    private val valueFactory: (Int) -> V,
) : DeviceParameter<V> {
    fun constructValue(intValue: Int): V = valueFactory(intValue)

    override fun rejectInvalidValue(value: V) {
        check(value in valueRange)
    }

    override fun coerceValid(value: V): V = value.coerceIn(valueRange)

    override fun buildUpdateMessage(value: V) = protocolAdapter.buildUpdateMessage(this, value)
    override fun applyToProgram(program: MutableProgram, value: V) = protocolAdapter.applyToProgram(program, this, value)
    override fun getValueFrom(program: Program): V = protocolAdapter.getValueFrom(program, this)
    override fun tryGetNewValueFromEvent(event: MessageToHost): V? = protocolAdapter.tryGetNewValueFromEvent(this, event)

    companion object {
        fun zeroToTenUnitless(
            id: DeviceParameter.Id<UnitlessSingleDecimalPrecision>,
            protocolAdapter: DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<UnitlessSingleDecimalPrecision>, UnitlessSingleDecimalPrecision>,
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
    private val protocolAdapter: DeviceParameter.ProtocolAdapter<DiscreteChoiceParameter<Value>, Value>,
    val choices: Set<Value>,
    override val default: Value,
) : DeviceParameter<Value> {
    override fun rejectInvalidValue(value: Value) {
        check(value in choices)
    }

    override fun coerceValid(value: Value): Value = if (value in choices) value else choices.first()

    override fun buildUpdateMessage(value: Value) = protocolAdapter.buildUpdateMessage(this, value)
    override fun applyToProgram(program: MutableProgram, value: Value) = protocolAdapter.applyToProgram(program, this, value)
    override fun getValueFrom(program: Program): Value = protocolAdapter.getValueFrom(program, this)
    override fun tryGetNewValueFromEvent(event: MessageToHost): Value? = protocolAdapter.tryGetNewValueFromEvent(this, event)

    companion object {
        inline operator fun <reified Value> invoke(
            id: DeviceParameter.Id<Value>,
            protocolAdapter: DeviceParameter.ProtocolAdapter<DiscreteChoiceParameter<Value>, Value>,
            default: Value,
        ): DiscreteChoiceParameter<Value>
            where Value : Enum<Value>, Value : SingleByteProtocolSerializable {
            return DiscreteChoiceParameter(id, protocolAdapter, enumValues<Value>().toSet(), default)
        }
    }
}

class BooleanParameter(
    override val id: DeviceParameter.Id<Boolean>,
    private val protocolAdapter: DeviceParameter.ProtocolAdapter<BooleanParameter, Boolean>,
    override val default: Boolean,
) : DeviceParameter<Boolean> {
    override fun rejectInvalidValue(value: Boolean) {
        // nothing to do
    }

    override fun coerceValid(value: Boolean) = value

    override fun buildUpdateMessage(value: Boolean) = protocolAdapter.buildUpdateMessage(this, value)
    override fun applyToProgram(program: MutableProgram, value: Boolean) = protocolAdapter.applyToProgram(program, this, value)
    override fun getValueFrom(program: Program): Boolean = protocolAdapter.getValueFrom(program, this)
    override fun tryGetNewValueFromEvent(event: MessageToHost): Boolean? = protocolAdapter.tryGetNewValueFromEvent(this, event)
}

private fun Continuous<*>.asZeroToTenDial() = ZeroToTenDial(intValue.toByte())
private fun Continuous<*>.asTwoByteDial() = TwoByteDial(intValue.toUShort())

private class ContinuousAmpDialProtocolAdapter<Value: Continuous<Value>, ProtocolValue : Any>(
    val dial: Byte,
    val field: KMutableProperty1<in MutableProgram, ProtocolValue>,
    val serializeToProtocol: (Value) -> ProtocolValue,
    val deserializeFromProtocol: (ProtocolValue) -> Value,
) : DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<Value>, Value> {
    override fun buildUpdateMessage(parameter: ContinuousRangeParameter<Value>, value: Value): MessageToAmp<*> {
        return AmpDialTurnedMessage(dial, value.asTwoByteDial())
    }

    override fun applyToProgram(program: MutableProgram, parameter: ContinuousRangeParameter<Value>, value: Value) {
        field.set(program, serializeToProtocol(value))
    }

    override fun getValueFrom(program: Program, parameter: ContinuousRangeParameter<Value>): Value {
        return field.get(program.unsafeCast<MutableProgram>()).let(deserializeFromProtocol)
    }

    override fun tryGetNewValueFromEvent(parameter: ContinuousRangeParameter<Value>, event: MessageToHost): Value? {
        if (event !is AmpDialTurnedMessage || event.dial != this.dial) {
            return null
        }

        return parameter.constructValue(event.value.semanticValue.toInt())
    }
}

private class ContinuousPedalDialProtocolAdapter<Value : Continuous<Value>, ProtocolValue : Any>(
    val slot: PedalSlot,
    val dial: Byte,
    val field: KMutableProperty1<in MutableProgram, ProtocolValue>,
    val serializeToProtocol: (Value) -> ProtocolValue,
    val deserializeFromProtocol: (ProtocolValue) -> Value,
) : DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<Value>, Value> {
    override fun buildUpdateMessage(parameter: ContinuousRangeParameter<Value>, value: Value): MessageToAmp<*> {
        return EffectDialTurnedMessage(slot, dial, value.asTwoByteDial())
    }

    override fun applyToProgram(program: MutableProgram, parameter: ContinuousRangeParameter<Value>, value: Value) {
        field.set(program, serializeToProtocol(value))
    }

    override fun getValueFrom(program: Program, parameter: ContinuousRangeParameter<Value>): Value {
        return field.get(program.unsafeCast<MutableProgram>()).let(deserializeFromProtocol)
    }

    override fun tryGetNewValueFromEvent(parameter: ContinuousRangeParameter<Value>, event: MessageToHost): Value? {
        if (event !is EffectDialTurnedMessage || event.pedalSlot != this.slot || event.dialIndex != this.dial) {
            return null
        }

        return parameter.constructValue(event.value.semanticValue.toInt())
    }
}

fun ampDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, ZeroToTenDial>,
): DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<UnitlessSingleDecimalPrecision>, UnitlessSingleDecimalPrecision> {
    return ContinuousAmpDialProtocolAdapter(index, field, Continuous<*>::asZeroToTenDial, { UnitlessSingleDecimalPrecision(it.value.toInt()) })
}

fun ampSwitch(
    index: Byte,
    field: KMutableProperty1<MutableProgram, Boolean>,
): DeviceParameter.ProtocolAdapter<DeviceParameter<Boolean>, Boolean> {
    return object : DeviceParameter.ProtocolAdapter<DeviceParameter<Boolean>, Boolean> {
        override fun buildUpdateMessage(parameter: DeviceParameter<Boolean>, value: Boolean): MessageToAmp<*> {
            return AmpDialTurnedMessage(index, TwoByteDial(if (value) 0x01u else 0x00u))
        }
        override fun applyToProgram(program: MutableProgram, parameter: DeviceParameter<Boolean>, value: Boolean) {
            field.set(program, value)
        }

        override fun getValueFrom(program: Program, parameter: DeviceParameter<Boolean>): Boolean {
            return field.get(program.unsafeCast<MutableProgram>())
        }

        override fun tryGetNewValueFromEvent(parameter: DeviceParameter<Boolean>, event: MessageToHost): Boolean? {
            if (event !is AmpDialTurnedMessage || event.dial != index) {
                return null
            }

            return when (val value = event.value.semanticValue.toInt()) {
                0x00 -> false
                0x01 -> true
                else -> error("Binary parameter ${parameter.id} got non-binary value in ${AmpDialTurnedMessage::class.simpleName}: $value")
            }
        }
    }
}

fun <Value : SingleByteProtocolSerializable> ampSelector(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, Value>,
): DeviceParameter.ProtocolAdapter<DiscreteChoiceParameter<Value>, Value> {
    return object : DeviceParameter.ProtocolAdapter<DiscreteChoiceParameter<Value>, Value> {
        override fun buildUpdateMessage(parameter: DiscreteChoiceParameter<Value>, value: Value) = AmpDialTurnedMessage(index, TwoByteDial(value.protocolValue.toUShort()))
        override fun applyToProgram(program: MutableProgram, parameter: DiscreteChoiceParameter<Value>, value: Value) {
            field.set(program, value)
        }

        override fun getValueFrom(program: Program, parameter: DiscreteChoiceParameter<Value>): Value {
            return field.get(program.unsafeCast<MutableProgram>())
        }

        override fun tryGetNewValueFromEvent(parameter: DiscreteChoiceParameter<Value>, event: MessageToHost): Value? {
            if (event !is AmpDialTurnedMessage || event.dial != index) {
                return null
            }

            val value = event.value.semanticValue.toByte()
            return parameter.choices.find { it.protocolValue == value }
                ?: error("Unknown value ${value.hex()} for amp parameter ${parameter.id}")
        }
    }
}

object NoiseReductionSensitivityProtocolAdapter : DeviceParameter.ProtocolAdapter<DeviceParameter<UnitlessSingleDecimalPrecision>, UnitlessSingleDecimalPrecision> {
    override fun buildUpdateMessage(parameter: DeviceParameter<UnitlessSingleDecimalPrecision>, value: UnitlessSingleDecimalPrecision) = NoiseReductionSensitivityChangedMessage(
        ZeroToTenDial(value.intValue.toByte())
    )

    override fun applyToProgram(program: MutableProgram, parameter: DeviceParameter<UnitlessSingleDecimalPrecision>, value: UnitlessSingleDecimalPrecision) {
        program.noiseReductionSensitivity = ZeroToTenDial(value.intValue.toByte())
    }

    override fun getValueFrom(program: Program, parameter: DeviceParameter<UnitlessSingleDecimalPrecision>): UnitlessSingleDecimalPrecision {
        return UnitlessSingleDecimalPrecision(program.noiseReductionSensitivity.value.toInt())
    }

    override fun tryGetNewValueFromEvent(
        parameter: DeviceParameter<UnitlessSingleDecimalPrecision>,
        event: MessageToHost
    ): UnitlessSingleDecimalPrecision? {
        if (event !is NoiseReductionSensitivityChangedMessage) {
            return null
        }

        return UnitlessSingleDecimalPrecision(event.sensitivity.value.toInt())
    }
}

fun PedalDescriptor<*>.pedalEnabledSwitch() : DeviceParameter.ProtocolAdapter<DeviceParameter<Boolean>, Boolean> {
    return object : DeviceParameter.ProtocolAdapter<DeviceParameter<Boolean>, Boolean> {
        override fun buildUpdateMessage(parameter: DeviceParameter<Boolean>, value: Boolean) = PedalActiveStateChangedMessage(pedalType.slot, value)

        override fun applyToProgram(program: MutableProgram, parameter: DeviceParameter<Boolean>, value: Boolean) {
            pedalType.slot.programEnabledField.set(program, value)
        }

        override fun getValueFrom(program: Program, parameter: DeviceParameter<Boolean>): Boolean {
            return pedalType.slot.programEnabledField.get(program.unsafeCast<MutableProgram>())
        }

        override fun tryGetNewValueFromEvent(parameter: DeviceParameter<Boolean>, event: MessageToHost): Boolean? {
            if (event !is PedalActiveStateChangedMessage || event.pedalSlot != pedalType.slot) {
                return null
            }

            return event.enabled
        }
    }
}

fun PedalDescriptor<*>.unitlessPedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, ZeroToTenDial>,
) : DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<UnitlessSingleDecimalPrecision>, UnitlessSingleDecimalPrecision> {
    return ContinuousPedalDialProtocolAdapter(
        pedalType.slot,
        index,
        field,
        UnitlessSingleDecimalPrecision::asZeroToTenDial,
        { UnitlessSingleDecimalPrecision(it.value.toInt()) },
    )
}

fun PedalDescriptor<*>.unitlessPedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, TwoByteDial>,
) : DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<UnitlessSingleDecimalPrecision>, UnitlessSingleDecimalPrecision> {
    return ContinuousPedalDialProtocolAdapter(
        pedalType.slot,
        index,
        field,
        UnitlessSingleDecimalPrecision::asTwoByteDial,
        { UnitlessSingleDecimalPrecision(it.semanticValue.toInt()) },
    )
}

fun PedalDescriptor<*>.unitlessPedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, Byte>,
) : DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<UnitlessSingleDecimalPrecision>, UnitlessSingleDecimalPrecision> {
    return ContinuousPedalDialProtocolAdapter(
        pedalType.slot,
        index,
        field,
        { it.intValue.toByte() },
        { UnitlessSingleDecimalPrecision(it.toInt()) },
    )
}

fun PedalDescriptor<*>.frequencyPedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, TwoByteDial>,
) : DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<Frequency>, Frequency> {
    return ContinuousPedalDialProtocolAdapter(
        pedalType.slot,
        index,
        field,
        { TwoByteDial(it.millihertz.toUShort()) },
        { Frequency(it.semanticValue.toInt()) }
    )
}

fun PedalDescriptor<*>.durationPedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, TwoByteDial>,
) : DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<Duration>, Duration> {
    return ContinuousPedalDialProtocolAdapter(
        pedalType.slot,
        index,
        field,
        { TwoByteDial(it.milliseconds.toUShort()) },
        { Duration(it.semanticValue.toInt()) }
    )
}

fun PedalDescriptor<*>.durationPedalDial(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, Byte>,
) : DeviceParameter.ProtocolAdapter<ContinuousRangeParameter<Duration>, Duration> {
    return ContinuousPedalDialProtocolAdapter(
        pedalType.slot,
        index,
        field,
        { it.milliseconds.toByte() },
        { Duration(it.toInt()) }
    )
}

fun <Value : SingleByteProtocolSerializable> PedalDescriptor<*>.pedalSelector(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, Byte>
) : DeviceParameter.ProtocolAdapter<DiscreteChoiceParameter<Value>, Value> {
    return object : DeviceParameter.ProtocolAdapter<DiscreteChoiceParameter<Value>, Value> {
        override fun buildUpdateMessage(parameter: DiscreteChoiceParameter<Value>, value: Value): MessageToAmp<*> {
            return EffectDialTurnedMessage(pedalType.slot, index, TwoByteDial(value.protocolValue.toUShort()))
        }

        override fun applyToProgram(program: MutableProgram, parameter: DiscreteChoiceParameter<Value>, value: Value) {
            field.set(program, value.protocolValue)
        }

        override fun getValueFrom(program: Program, parameter: DiscreteChoiceParameter<Value>): Value {
            val byteValue = field.get(program.unsafeCast<MutableProgram>())
            return parameter.choices.find { it.protocolValue == byteValue }
                ?: error("Unknown value ${byteValue.hex()} for parameter $pedalType ${parameter.id}")
        }

        override fun tryGetNewValueFromEvent(parameter: DiscreteChoiceParameter<Value>, event: MessageToHost): Value? {
            if (event !is EffectDialTurnedMessage || event.pedalSlot != pedalType.slot || event.dialIndex != index) {
                return null
            }

            val value = event.value.semanticValue.toByte()
            return parameter.choices.find { it.protocolValue == value }
                ?: error("Unknown value ${value.hex()} for parameter $pedalType ${parameter.id}")
        }
    }
}

fun PedalDescriptor<*>.pedalSwitch(
    index: Byte,
    field: KMutableProperty1<in MutableProgram, Byte>,
) : DeviceParameter.ProtocolAdapter<DeviceParameter<Boolean>, Boolean> {
    return object : DeviceParameter.ProtocolAdapter<DeviceParameter<Boolean>, Boolean> {
        override fun buildUpdateMessage(parameter: DeviceParameter<Boolean>, value: Boolean): MessageToAmp<*> {
            return EffectDialTurnedMessage(pedalType.slot, index, TwoByteDial(if (value) 0x01u else 0x00u))
        }

        override fun applyToProgram(program: MutableProgram, parameter: DeviceParameter<Boolean>, value: Boolean) {
            field.set(program, if (value) 0x01 else 0x00)
        }

        override fun getValueFrom(program: Program, parameter: DeviceParameter<Boolean>): Boolean {
            return when (val value = field.get(program.unsafeCast<MutableProgram>()).toInt()) {
                0x00 -> true
                0x01 -> false
                else -> error("Binary parameter does not have binary value: $field = $value")
            }
        }

        override fun tryGetNewValueFromEvent(parameter: DeviceParameter<Boolean>, event: MessageToHost): Boolean? {
            if (event !is EffectDialTurnedMessage || event.pedalSlot != pedalType.slot || event.dialIndex != index) {
                return null
            }

            return when (val value = event.value.semanticValue.toInt()) {
                0x00 -> false
                0x01 -> true
                else -> error("Binary parameter ${parameter.id} got non-binary value in ${EffectDialTurnedMessage::class.simpleName}: $value")
            }
        }
    }
}