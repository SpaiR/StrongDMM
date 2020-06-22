package strongdmm.service.preferences.prefs

import strongdmm.service.preferences.data.BooleanData

abstract class PreferenceBoolean(
    private val value: BooleanData = BooleanData()
) : Preference<BooleanData>() {
    constructor(value: Boolean) : this(BooleanData(value))

    override fun getValue(): BooleanData = value
}
