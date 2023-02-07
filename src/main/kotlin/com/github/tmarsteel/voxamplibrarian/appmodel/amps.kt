package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass
import com.github.tmarsteel.voxamplibrarian.protocol.TubeBias

abstract class AmplifierDescriptor(
    override val name: String,
    val supportsBrightCap: Boolean,
    val presenceIsCalledTone: Boolean = false,
) : DeviceDescriptor {
    final override val parameters = ALL_AMP_PARAMETERS + listOfNotNull(
        BRIGHT_CAP_PARAMETER.takeIf { supportsBrightCap },
        if (presenceIsCalledTone) TONE_PARAMETER else PRESENCE_PARAMETER,
    )

    override val defaults: Map<DeviceParameter.Id, Any> = ALL_AMP_DEFAULTS + listOfNotNull(
        (DeviceParameter.Id.AMP_BRIGHT_CAP to true).takeIf { supportsBrightCap },
        if (presenceIsCalledTone) (DeviceParameter.Id.AMP_TONE to 20) else (DeviceParameter.Id.AMP_PRESENCE to 20),
    )

    companion object {
        val BRIGHT_CAP_PARAMETER = BooleanParameter(DeviceParameter.Id.AMP_BRIGHT_CAP)
        val PRESENCE_PARAMETER = ContinuousRangeParameter(DeviceParameter.Id.AMP_PRESENCE)
        val TONE_PARAMETER = ContinuousRangeParameter(DeviceParameter.Id.AMP_TONE)
        val ALL_AMP_PARAMETERS = listOf(
            ContinuousRangeParameter(DeviceParameter.Id.GAIN),
            ContinuousRangeParameter(DeviceParameter.Id.EQ_BASS),
            ContinuousRangeParameter(DeviceParameter.Id.EQ_MIDDLE),
            ContinuousRangeParameter(DeviceParameter.Id.EQ_TREBLE),
            ContinuousRangeParameter(DeviceParameter.Id.AMP_VOLUME),
            ContinuousRangeParameter(DeviceParameter.Id.RESONANCE),
            ContinuousRangeParameter(DeviceParameter.Id.AMP_NOISE_REDUCTION_SENSITIVITY),
            BooleanParameter(DeviceParameter.Id.AMP_LOW_CUT),
            BooleanParameter(DeviceParameter.Id.AMP_MID_BOOST),
            DiscreteChoiceParameter<TubeBias>(DeviceParameter.Id.AMP_TUBE_BIAS),
            DiscreteChoiceParameter<AmpClass>(DeviceParameter.Id.AMP_CLASS)
        )

        val ALL_AMP_DEFAULTS = mapOf(
            DeviceParameter.Id.GAIN to 50,
            DeviceParameter.Id.EQ_BASS to 50,
            DeviceParameter.Id.EQ_MIDDLE to 50,
            DeviceParameter.Id.EQ_TREBLE to 50,
            DeviceParameter.Id.AMP_VOLUME to 50,
            DeviceParameter.Id.RESONANCE to 75,
            DeviceParameter.Id.AMP_NOISE_REDUCTION_SENSITIVITY to 30,
            DeviceParameter.Id.AMP_LOW_CUT to false,
            DeviceParameter.Id.AMP_MID_BOOST to false,
            DeviceParameter.Id.AMP_BRIGHT_CAP to true,
            DeviceParameter.Id.AMP_TUBE_BIAS to TubeBias.OFF,
            DeviceParameter.Id.AMP_CLASS to AmpClass.A,
        )

        val DEFAULT: DeviceConfiguration<AmplifierDescriptor> = DeviceConfiguration(
            VoxAc30Amplifier,
            VoxAc30Amplifier.defaults,
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