package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.dme.isType

object LayersManager {

    private val hiddenTypes = mutableSetOf<String>()

    fun toggleType(type: String) {
        if (!hiddenTypes.add(type)) {
            hiddenTypes.remove(type)
        }
        Frame.update(true)
    }

    fun isHiddenType(type: String): Boolean {
        for (hiddenType in hiddenTypes) {
            if (isType(type, hiddenType)) {
                return true
            }
        }
        return false
    }
}
