package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter

val DeviceParameter.Id<*>.label: String get() = when (this) {
    DeviceParameter.Id.Gain -> "Gain"
    DeviceParameter.Id.EqBass -> "Bass"
    DeviceParameter.Id.EqMiddle -> "Middle"
    DeviceParameter.Id.EqTreble -> "Treble"
    DeviceParameter.Id.EqLowCut -> "Low Cut"
    DeviceParameter.Id.EqHighCut -> "High Cut"
    DeviceParameter.Id.EqTone -> "Tone"
    DeviceParameter.Id.AmpVolume -> "Volume"
    DeviceParameter.Id.AmpPresence -> "Presence"
    DeviceParameter.Id.Resonance -> "Resonance"
    DeviceParameter.Id.AmpTone -> "Tone"
    DeviceParameter.Id.AmpTubeBias -> "Tube Bias"
    DeviceParameter.Id.AmpClass -> "Class"
    DeviceParameter.Id.AmpLowCut -> "Low Cut"
    DeviceParameter.Id.AmpMidBoost -> "Mid Boost"
    DeviceParameter.Id.AmpBrightCap -> "Bright Cap"
    DeviceParameter.Id.AmpNoiseReductionSensitivity -> "Noise-Reduction Sensitivity"
    DeviceParameter.Id.PedalEnabled -> "Enabled"
    DeviceParameter.Id.PedalLevel -> "Level"
    DeviceParameter.Id.PedalMix -> "Mix"
    DeviceParameter.Id.ModulationSpeed -> "Speed"
    DeviceParameter.Id.DelayModulationSpeed -> "Speed"
    DeviceParameter.Id.ModulationDepth -> "Depth"
    DeviceParameter.Id.ModulationManual -> "Manual"
    DeviceParameter.Id.CompSensitivity -> "Sensitivity"
    DeviceParameter.Id.CompAttack -> "Attack"
    DeviceParameter.Id.CompVoice -> "Voice"
    DeviceParameter.Id.OverdriveDrive -> "Drive"
    DeviceParameter.Id.TremoloDuty -> "Duty"
    DeviceParameter.Id.TremoloShape -> "Shape"
    DeviceParameter.Id.DelayTime -> "Time"
    DeviceParameter.Id.DelayFeedback -> "Feedback"
    DeviceParameter.Id.ReverbTime -> "Time"
    DeviceParameter.Id.ReverbPreDelay -> "Pre-Delay"
    DeviceParameter.Id.ReverbLowDamp -> "Low Damp"
    DeviceParameter.Id.ReverbHighDamp -> "High Damp"
}

val ParameterOrder : Comparator<DeviceParameter<*>> = object : Comparator<DeviceParameter<*>> {
    val order: Map<DeviceParameter.Id<*>, Int> = listOf(
        // PEDALS
        DeviceParameter.Id.PedalEnabled,

        DeviceParameter.Id.CompSensitivity,
        DeviceParameter.Id.CompAttack,
        DeviceParameter.Id.CompVoice,

        DeviceParameter.Id.ModulationSpeed,
        DeviceParameter.Id.ModulationDepth,
        DeviceParameter.Id.ModulationManual,

        DeviceParameter.Id.TremoloDuty,
        DeviceParameter.Id.TremoloShape,

        DeviceParameter.Id.OverdriveDrive,

        DeviceParameter.Id.EqTone,
        DeviceParameter.Id.EqLowCut,
        DeviceParameter.Id.EqHighCut,

        DeviceParameter.Id.PedalMix,
        DeviceParameter.Id.PedalLevel,

        DeviceParameter.Id.DelayModulationSpeed,


        DeviceParameter.Id.DelayTime,
        DeviceParameter.Id.DelayFeedback,
        DeviceParameter.Id.ReverbTime,
        DeviceParameter.Id.ReverbPreDelay,
        DeviceParameter.Id.ReverbLowDamp,
        DeviceParameter.Id.ReverbHighDamp,

        // AMP
        DeviceParameter.Id.Gain,
        DeviceParameter.Id.EqTreble,
        DeviceParameter.Id.EqMiddle,
        DeviceParameter.Id.EqBass,
        DeviceParameter.Id.AmpVolume,
        DeviceParameter.Id.AmpTone,
        DeviceParameter.Id.AmpPresence,
        DeviceParameter.Id.Resonance,
        DeviceParameter.Id.AmpNoiseReductionSensitivity,
        DeviceParameter.Id.AmpTubeBias,
        DeviceParameter.Id.AmpClass,
        DeviceParameter.Id.AmpBrightCap,
        DeviceParameter.Id.AmpMidBoost,
        DeviceParameter.Id.AmpLowCut,
    )
        .mapIndexed { index, id -> Pair(index, id) }
        .associate { (index, id) -> id to index }

    override fun compare(a: DeviceParameter<*>, b: DeviceParameter<*>): Int {
        return order.getValue(a.id).compareTo(order.getValue(b.id))
    }
}