package strongdmm.service.preferences.prefs.enums

import strongdmm.service.preferences.prefs.PreferenceEnum

class NudgeMode private constructor(value: String) : PreferenceEnum(value) {
    companion object {
        val PIXEL: NudgeMode =
            NudgeMode("PIXEL")
        val STEP: NudgeMode =
            NudgeMode("STEP")

        val enums: Array<PreferenceEnum> = arrayOf(
            PIXEL,
            STEP
        )
    }

    constructor() : this("PIXEL")

    override fun getGroup(): String = "Save Options"
    override fun getHeader(): String = "Nudge Mode"
    override fun getDesc(): String = "Controls which variables will be changed during the nudge."
    override fun getLabel(): String = "##nudge_mode"

    override fun getEnums(): Array<PreferenceEnum> {
        return enums
    }

    override fun getReadableName(): String {
        return if (this == PIXEL) "pixel_x/pixel_y" else "step_x/step_y"
    }
}
