package strongdmm.service.preferences.prefs.ints

import strongdmm.service.preferences.prefs.PreferenceInteger
import strongdmm.application.window.Window

class InterfaceScalePercent(value: Int) : PreferenceInteger(value) {
    constructor() : this(100)

    override fun getGroup(): String = "Interface Options"
    override fun getHeader(): String = "Scale"
    override fun getDesc(): String = "Controls the interface scale."
    override fun getLabel(): String = "%"
    override fun getMin(): Int = 50
    override fun getMax(): Int = 250

    override fun applyModify() {
        Window.newPointSize = getValue().data / 100f
    }
}
