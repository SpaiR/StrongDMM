package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.common.isType
import io.github.spair.strongdmm.gui.map.Frame

object LayersManager {

    private val hiddenTypes: MutableSet<String> = mutableSetOf()
    private val visibleTypes: MutableSet<String> = mutableSetOf()

    fun toggleType(type: String) {
        if (!hiddenTypes.add(type)) {
            hiddenTypes.remove(type)
        }
        Frame.update(true)
    }

    fun addHiddenType(type: String): Boolean = hiddenTypes.add(type)
    fun removeHiddenType(type: String): Boolean = hiddenTypes.remove(type)

    fun addVisibleType(type: String): Boolean = visibleTypes.add(type)
    fun removeVisibleType(type: String): Boolean = visibleTypes.remove(type)

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

    fun isInHiddenTypes(type: String): Boolean = hiddenTypes.contains(type)
    fun isInVisibleTypes(type: String): Boolean = visibleTypes.contains(type)

    fun reset() {
        hiddenTypes.clear()
        visibleTypes.clear()
    }
}
