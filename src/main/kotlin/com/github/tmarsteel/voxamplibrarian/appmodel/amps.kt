package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass
import com.github.tmarsteel.voxamplibrarian.protocol.AmpModel
import com.github.tmarsteel.voxamplibrarian.protocol.MutableProgram
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.TubeBias
import com.github.tmarsteel.voxamplibrarian.protocol.message.SimulatedAmpModelChangedMessage

abstract class AmplifierDescriptor(
    override val name: String,
    val protocolModel: AmpModel,
    val supportsBrightCap: Boolean,
    val presenceIsCalledTone: Boolean = false,
) : DeviceDescriptor<AmplifierDescriptor> {
    override val parameters = ALL_AMP_PARAMETERS + listOfNotNull(
        BRIGHT_CAP_PARAMETER .takeIf { supportsBrightCap },
        if (presenceIsCalledTone) TONE_PARAMETER else PRESENCE_PARAMETER,
    )

    override val typeChangedMessage = SimulatedAmpModelChangedMessage(protocolModel)

    override fun applyTypeToProgram(program: MutableProgram) {
        program.ampModel = protocolModel
    }

    override fun isContainedIn(program: Program): Boolean {
        return program.ampModel == protocolModel
    }

    companion object {
        val BRIGHT_CAP_PARAMETER = BooleanParameter(
            DeviceParameter.Id.AmpBrightCap,
            ampSwitch(0x07, MutableProgram::brightCap),
            true
        )
        val PRESENCE_PARAMETER = ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.AmpPresence,
            ampSelector(0x05, MutableProgram::presence),
            2.0
        )
        val TONE_PARAMETER = ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.AmpTone,
            ampSelector(0x05, MutableProgram::presence),
            2.0
        )

        val ALL_AMP_PARAMETERS = listOf<DeviceParameter<*>>(
            ContinuousRangeParameter.zeroToTenUnitless(
                DeviceParameter.Id.Gain,
                ampSelector(0x00, MutableProgram::gain),
                5.0
            ),
            ContinuousRangeParameter.zeroToTenUnitless(
                DeviceParameter.Id.EqBass,
                ampSelector(0x01, MutableProgram::bass),
                5.0
            ),
            ContinuousRangeParameter.zeroToTenUnitless(
                DeviceParameter.Id.EqMiddle,
                ampSelector(0x02, MutableProgram::middle),
                5.0
            ),
            ContinuousRangeParameter.zeroToTenUnitless(
                DeviceParameter.Id.EqTreble,
                ampSelector(0x03, MutableProgram::treble),
                5.0
            ),
            ContinuousRangeParameter.zeroToTenUnitless(
                DeviceParameter.Id.AmpVolume,
                ampSelector(0x04, MutableProgram::volume),
                5.0
            ),
            ContinuousRangeParameter.zeroToTenUnitless(
                DeviceParameter.Id.Resonance,
                ampSelector(0x06, MutableProgram::resonance),
                7.5
            ),
            ContinuousRangeParameter.zeroToTenUnitless(
                DeviceParameter.Id.AmpNoiseReductionSensitivity,
                NoiseReductionSensitivityProtocolAdapter,
                3.0
            ),
            BooleanParameter(
                DeviceParameter.Id.AmpLowCut,
                ampSwitch(0x08, MutableProgram::lowCut),
                false
            ),
            BooleanParameter(
                DeviceParameter.Id.AmpMidBoost,
                ampSwitch(0x09, MutableProgram::midBoost),
                false
            ),
            DiscreteChoiceParameter(
                DeviceParameter.Id.AmpTubeBias,
                ampSelector(0x0A, MutableProgram::tubeBias),
                TubeBias.OFF
            ),
            DiscreteChoiceParameter(
                DeviceParameter.Id.AmpClass,
                ampSelector(0x0B, MutableProgram::ampClass),
                AmpClass.A
            ),
        )

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
    protocolModel = AmpModel.DELUXE_CL_VIBRATO,
    supportsBrightCap = true,
)

object DeluxeClNormalAmplifier : AmplifierDescriptor(
    name = "Fender '65 Deluxe Reverb Normal Channel",
    protocolModel = AmpModel.DELUXE_CL_NORMAL,
    supportsBrightCap = false,
)

object Tweed4X10BrightAmplifier : AmplifierDescriptor(
    name = "Fender Bassman 4x10 Bright Channel",
    protocolModel = AmpModel.TWEED_410_BRIGHT,
    supportsBrightCap = true,
)

object Tweed4X10NormalAmplifier : AmplifierDescriptor(
    name = "Fender Bassman 4x10 Normal Channel",
    protocolModel = AmpModel.TWEED_410_NORMAL,
    supportsBrightCap = false,
)

object BoutiqueClAmplifier : AmplifierDescriptor(
    name = "Overdrive Special Clean Channel",
    protocolModel = AmpModel.BOUTIQUE_CL,
    supportsBrightCap = true,
)

object BoutiqueOdAmplifier : AmplifierDescriptor(
    name = "Overdrive Special Overdrive Channel",
    protocolModel = AmpModel.BOUTIQUE_OD,
    supportsBrightCap = true,
)

object VoxAc30Amplifier : AmplifierDescriptor(
    name = "VOX AC30",
    protocolModel = AmpModel.VOX_AC30,
    supportsBrightCap = true,
    presenceIsCalledTone = true,
)

object VoxAc30TbAmplifier : AmplifierDescriptor(
    name = "VOX AC30TB",
    protocolModel = AmpModel.VOX_AC30TB,
    supportsBrightCap = true,
    presenceIsCalledTone = true,
)

object Brit1959TrebleAmplifier : AmplifierDescriptor(
    name = "Marshal JTM Treble",
    protocolModel = AmpModel.BRIT_1959_TREBLE,
    supportsBrightCap = true,
)

object Brit1959NormalAmplifier : AmplifierDescriptor(
    name = "Marshal JTM Normal",
    protocolModel = AmpModel.BRIT_1959_NORMAL,
    supportsBrightCap = false,
)

object Brit800Amplifier : AmplifierDescriptor(
    name = "Marshal JCM-800",
    protocolModel = AmpModel.BRIT_800,
    supportsBrightCap = true,
)

object BritVmAmplifier : AmplifierDescriptor(
    name = "Marshal JVM-410",
    protocolModel = AmpModel.BRIT_VM,
    supportsBrightCap = true,
)

object SlOdAmplifier : AmplifierDescriptor(
    name = "Soldano SLO-100",
    protocolModel = AmpModel.SL_OD,
    supportsBrightCap = true,
)

object DoubleRecAmplifier : AmplifierDescriptor(
    name = "Mesa Boogie Dual Rectifier",
    protocolModel = AmpModel.DOUBLE_REC,
    supportsBrightCap = true,
)

object CaliElationAmplifier : AmplifierDescriptor(
    name = "Cali Elation",
    protocolModel = AmpModel.CALI_ELATION,
    supportsBrightCap = true,
)

object EruptThreeChannelTwoAmplifier : AmplifierDescriptor(
    name = "Peavy 5150 III Channel 2",
    protocolModel = AmpModel.ERUPT_III_CH2,
    supportsBrightCap = false,
)

object EruptThreeChannelThreeAmplifier : AmplifierDescriptor(
    name = "Peavy 5150 III Channel 3",
    protocolModel = AmpModel.ERUPT_III_CH3,
    supportsBrightCap = true,
)

object BoutiqueMetalAmplifier : AmplifierDescriptor(
    name = "Diezel VH4",
    protocolModel = AmpModel.BOUTIQUE_METAL,
    supportsBrightCap = false,
)

object BritOrMkTwoAmplifier : AmplifierDescriptor(
    name = "Orange Super Crush 100",
    protocolModel = AmpModel.BRIT_OR_MKII,
    supportsBrightCap = true,
)

object OriginalCleanAmplifier : AmplifierDescriptor(
    name = "No additional simulation, just the VTX amp",
    protocolModel = AmpModel.ORIGINAL_CL,
    supportsBrightCap = true,
)