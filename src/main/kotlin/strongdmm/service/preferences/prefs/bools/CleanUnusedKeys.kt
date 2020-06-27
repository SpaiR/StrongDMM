package strongdmm.service.preferences.prefs.bools

import strongdmm.service.preferences.prefs.PreferenceBoolean

class CleanUnusedKeys(value: Boolean) : PreferenceBoolean(value) {
    constructor() : this(true)

    override fun getGroup(): String = "Save Options"
    override fun getHeader(): String = "Clean Unused Keys"
    override fun getDesc(): String = "When enabled, content tile keys which are not used on the map will be removed."
    override fun getLabel(): String = "##clean_unused_keys"
}
