package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.ParameterValue.Companion.withValue
import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass
import com.github.tmarsteel.voxamplibrarian.protocol.TubeBias

abstract class AmplifierDescriptor(
    name: String,
    val supportsBrightCap: Boolean,
    val presenceIsCalledTone: Boolean = false,
) : DeviceDescriptor(
    name,
    ALL_AMP_DEFAULTS + listOfNotNull(
        (BRIGHT_CAP_PARAMETER withValue true) .takeIf { supportsBrightCap },
        (if (presenceIsCalledTone) TONE_PARAMETER else PRESENCE_PARAMETER) withValue 20,
    )
) {
    companion object {
        val BRIGHT_CAP_PARAMETER = BooleanParameter(DeviceParameter.Id.AmpBrightCap)
        val PRESENCE_PARAMETER = ContinuousRangeParameter(DeviceParameter.Id.AmpPresence)
        val TONE_PARAMETER = ContinuousRangeParameter(DeviceParameter.Id.AmpTone)

        val ALL_AMP_DEFAULTS = listOf<ParameterValue<*>>(
            ContinuousRangeParameter(DeviceParameter.Id.Gain) withValue 50,
            ContinuousRangeParameter(DeviceParameter.Id.EqBass) withValue 50,
            ContinuousRangeParameter(DeviceParameter.Id.EqMiddle) withValue 50,
            ContinuousRangeParameter(DeviceParameter.Id.EqTreble) withValue 50,
            ContinuousRangeParameter(DeviceParameter.Id.AmpVolume) withValue 50,
            ContinuousRangeParameter(DeviceParameter.Id.Resonance) withValue 75,
            ContinuousRangeParameter(DeviceParameter.Id.AmpNoiseReductionSensitivity) withValue 30,
            BooleanParameter(DeviceParameter.Id.AmpLowCut) withValue false,
            BooleanParameter(DeviceParameter.Id.AmpMidBoost) withValue false,
            DiscreteChoiceParameter(DeviceParameter.Id.AmpTubeBias) withValue TubeBias.OFF,
            DiscreteChoiceParameter(DeviceParameter.Id.AmpClass) withValue AmpClass.A,
        )

        val DEFAULT: DeviceConfiguration<AmplifierDescriptor> = DeviceConfiguration.defaultOf(VoxAc30Amplifier)

        val ALL: List<AmplifierDescriptor> = listOf(
            DeluxeClNormalAmplifier,
            DeluxeClVibratoAmplifier,
            Tweed4X10BrightAmplifier,
            Tweed4X10NormalAmplifier,
            BoutiqueClAmplifier,
            BoutiqueOdAmplifier,
            VoxAc30Amplifier,
            VoxAc30TbAmplifier,
            Brit1959TrebleAmplifier,
            Brit1959NormalAmplifier,
            Brit800Amplifier,
            BritVmAmplifier,
            SlOdAmplifier,
            DoubleRecAmplifier,
            CaliElationAmplifier,
            EruptThreeChannelTwoAmplifier,
            EruptThreeChannelThreeAmplifier,
            BoutiqueMetalAmplifier,
            BritOrMkTwoAmplifier,
            OriginalCleanAmplifier,
        )
    }
}

object DeluxeClVibratoAmplifier : AmplifierDescriptor(
    name = "Fender '65 Deluxe Reverb Vibrato Channel",
    supportsBrightCap = true,
)

object DeluxeClNormalAmplifier : AmplifierDescriptor(
    name = "Fender '65 Deluxe Reverb Normal Channel",
    supportsBrightCap = false,
)

object Tweed4X10BrightAmplifier : AmplifierDescriptor(
    name = "Fender Bassman 4x10 Bright Channel",
    supportsBrightCap = true,
)

object Tweed4X10NormalAmplifier : AmplifierDescriptor(
    name = "Fender Bassman 4x10 Normal Channel",
    supportsBrightCap = false,
)

object BoutiqueClAmplifier : AmplifierDescriptor(
    name = "Overdrive Special Clean Channel",
    supportsBrightCap = true,
)

object BoutiqueOdAmplifier : AmplifierDescriptor(
    name = "Overdrive Special Overdrive Channel",
    supportsBrightCap = true,
)

object VoxAc30Amplifier : AmplifierDescriptor(
    name = "VOX AC30",
    supportsBrightCap = true,
    presenceIsCalledTone = true,
)

object VoxAc30TbAmplifier : AmplifierDescriptor(
    name = "VOX AC30TB",
    supportsBrightCap = true,
    presenceIsCalledTone = true,
)

object Brit1959TrebleAmplifier : AmplifierDescriptor(
    name = "Marshal JTM Treble",
    supportsBrightCap = true,
)

object Brit1959NormalAmplifier : AmplifierDescriptor(
    name = "Marshal JTM Normal",
    supportsBrightCap = false,
)

object Brit800Amplifier : AmplifierDescriptor(
    name = "Marshal JCM-800",
    supportsBrightCap = true,
)

object BritVmAmplifier : AmplifierDescriptor(
    name = "Marshal JVM-410",
    supportsBrightCap = true,
)

object SlOdAmplifier : AmplifierDescriptor(
    name = "Soldano SLO-100",
    supportsBrightCap = true,
)

object DoubleRecAmplifier : AmplifierDescriptor(
    name = "Mesa Boogie Dual Rectifier",
    supportsBrightCap = true,
)

object CaliElationAmplifier : AmplifierDescriptor(
    name = "Cali Elation",
    supportsBrightCap = true,
)

object EruptThreeChannelTwoAmplifier : AmplifierDescriptor(
    name = "Peavy 5150 III Channel 2",
    supportsBrightCap = false,
)

object EruptThreeChannelThreeAmplifier : AmplifierDescriptor(
    name = "Peavy 5150 III Channel 3",
    supportsBrightCap = true,
)

object BoutiqueMetalAmplifier : AmplifierDescriptor(
    name = "Diezel VH4",
    supportsBrightCap = false,
)

object BritOrMkTwoAmplifier : AmplifierDescriptor(
    name = "Orange Super Crush 100",
    supportsBrightCap = true,
)

object OriginalCleanAmplifier : AmplifierDescriptor(
    name = "No additional simulation, just the VTX amp",
    supportsBrightCap = true,
)