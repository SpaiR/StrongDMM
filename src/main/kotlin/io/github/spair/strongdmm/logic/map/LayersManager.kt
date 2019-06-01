package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.dme.isType

object LayersManager {

    private val hiddenTypes = mutableSetOf<String>()
    private val visibleTypes = mutableSetOf<String>()

    fun toggleType(type: String) {
        if (!hiddenTypes.add(type)) {
            hiddenTypes.remove(type)
        }
        Frame.update(true)
    }

    fun addHiddenType(type: String) = hiddenTypes.add(type)
    fun removeHiddenType(type: String) = hiddenTypes.remove(type)

    fun addVisibleType(type: String) = visibleTypes.add(type)
    fun removeVisibleType(type: String) = visibleTypes.remove(type)

    fun isHiddenType(type: String): Boolean {
        if (visibleTypes.contains(type)) {
            return false
        }
        for (hiddenType in hiddenTypes) {
            if (isType(type, hiddenType)) {
                return true
            }
        }
        return false
    }

    fun isInHiddenTypes(type: String) = hiddenTypes.contains(type)
    fun isInVisibleTypes(type: String) = visibleTypes.contains(type)

    fun reset() {
        hiddenTypes.clear()
        visibleTypes.clear()
    }
}
