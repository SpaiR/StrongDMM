package strongdmm.service.preferences.prefs.enums

import strongdmm.service.preferences.prefs.PreferenceEnum
import strongdmm.window.AppWindowStyle

class StyleMode private constructor(value: String) : PreferenceEnum(value) {
    companion object {
        val PEACEFUL_SPACE: StyleMode = StyleMode("PEACEFUL_SPACE")
        val DARK_COAST: StyleMode = StyleMode("DARK_COAST")
        val MONOCHROME_DARKNESS: StyleMode = StyleMode("MONOCHROME_DARKNESS")
        val CRIMSON_MOON: StyleMode = StyleMode("CRIMSON_MOON")

        val enums: Array<PreferenceEnum> = arrayOf(
            PEACEFUL_SPACE,
            DARK_COAST,
            MONOCHROME_DARKNESS,
            CRIMSON_MOON
        )
    }

    constructor() : this(PEACEFUL_SPACE.getValue().data)

    override fun getGroup(): String = "Interface Options"
    override fun getHeader(): String = "Style"
    override fun getDesc(): String = "Controls the interface color schema."
    override fun getLabel(): String = "##interface_style"

    override fun getEnums(): Array<PreferenceEnum> {
        return enums
    }

    override fun getReadableName(): String {
        return when (this) {
            PEACEFUL_SPACE -> "Peaceful Space"
            DARK_COAST -> "Dark Coast"
            MONOCHROME_DARKNESS -> "Monochrome Darkness"
            CRIMSON_MOON -> "Crimson Moon"
            else -> "Unknown Style"
        }
    }

    override fun applyModify() {
        when (this) {
            PEACEFUL_SPACE -> AppWindowStyle.setPeacefulSpace()
            DARK_COAST -> AppWindowStyle.setDarkCoast()
            MONOCHROME_DARKNESS -> AppWindowStyle.setUnrealDarkness()
            CRIMSON_MOON -> AppWindowStyle.setCrimsonMoon()
        }
    }
}
