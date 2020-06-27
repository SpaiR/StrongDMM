package strongdmm.service.preferences.prefs

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonUnwrapped

abstract class Preference<T> {
    @JsonUnwrapped
    abstract fun getValue(): T

    @JsonIgnore
    abstract fun getGroup(): String
    @JsonIgnore
    abstract fun getHeader(): String
    @JsonIgnore
    abstract fun getDesc(): String
    @JsonIgnore
    abstract fun getLabel(): String

    open fun applyModify() {
        // Do nothing
    }

    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Preference<T>
        return getValue() == other.getValue()
    }

    override fun hashCode(): Int {
        return getValue().hashCode()
    }
}
