package strongdmm.service.preferences.prefs

import com.fasterxml.jackson.annotation.JsonIgnore
import strongdmm.service.preferences.data.IntegerData

abstract class PreferenceInteger(
    private val value: IntegerData = IntegerData()
) : Preference<IntegerData>() {
    constructor(value: Int) : this(IntegerData(value))

    override fun getValue(): IntegerData = value

    @JsonIgnore
    open fun getMin(): Int = Int.MIN_VALUE
    @JsonIgnore
    open fun getMax(): Int = Int.MAX_VALUE

    @JsonIgnore
    open fun getStep(): Int = 1
    @JsonIgnore
    open fun getStepFast(): Int = 10
}
