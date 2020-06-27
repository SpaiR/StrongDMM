package strongdmm.service.preferences.prefs

import com.fasterxml.jackson.annotation.JsonIgnore
import strongdmm.service.preferences.data.StringData

abstract class PreferenceEnum(
    private val value: StringData = StringData()
) : Preference<StringData>() {
    constructor(value: String) : this(StringData(value))

    override fun getValue(): StringData = value

    @JsonIgnore
    open fun getEnums(): Array<PreferenceEnum> = emptyArray()

    @JsonIgnore
    open fun getReadableName(): String = toString()
}
