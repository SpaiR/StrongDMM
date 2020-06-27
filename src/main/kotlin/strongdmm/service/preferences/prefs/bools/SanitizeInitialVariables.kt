package strongdmm.service.preferences.prefs.bools

import strongdmm.service.preferences.prefs.PreferenceBoolean

class SanitizeInitialVariables(value: Boolean) : PreferenceBoolean(value) {
    constructor() : this(false)

    override fun getGroup(): String = "Save Options"
    override fun getHeader(): String = "Sanitize Variables"
    override fun getDesc(): String = "Enables sanitizing for variables which are declared on the map, but the same as default."
    override fun getLabel(): String = "##sanitize_variables"
}
