package strongdmm.service.preferences.prefs.enums

import strongdmm.service.preferences.prefs.PreferenceEnum

class MapSaveMode private constructor(value: String) : PreferenceEnum(value) {
    companion object {
        val PROVIDED: MapSaveMode =
            MapSaveMode("PROVIDED")
        val BYOND: MapSaveMode =
            MapSaveMode("BYOND")
        val TGM: MapSaveMode =
            MapSaveMode("TGM")

        val enums: Array<PreferenceEnum> = arrayOf(
            PROVIDED,
            BYOND,
            TGM
        )
    }

    constructor() : this("PROVIDED")

    override fun getGroup(): String = "Save Options"
    override fun getHeader(): String = "Map Save Format"
    override fun getDesc(): String = "Controls the format used by the editor to save the map."
    override fun getLabel(): String = "##map_save_format"

    override fun getEnums(): Array<PreferenceEnum> {
        return enums
    }

    override fun getReadableName(): String {
        return getValue().data.toLowerCase().capitalize()
    }
}
