package strongdmm.service.preferences.prefs.bools

import strongdmm.service.preferences.prefs.PreferenceBoolean

class AlternativeScrollBehavior(value: Boolean) : PreferenceBoolean(value) {
    constructor() : this(false)

    override fun getGroup(): String = "Controls Options"
    override fun getHeader(): String = "Alternative Scroll Behavior"
    override fun getDesc(): String = "When enabled, scrolling will do panning. Zoom will be available with the pressed Space button."
    override fun getLabel(): String = "##alternative_scroll_behavior"
}
