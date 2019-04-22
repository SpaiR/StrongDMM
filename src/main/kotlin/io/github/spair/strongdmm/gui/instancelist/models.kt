package io.github.spair.strongdmm.gui.instancelist

import io.github.spair.strongdmm.logic.dmi.SOUTH

data class ListItemInstance(
    val name: String,
    val icon: String,
    val iconState: String,
    val dir: Int = SOUTH,
    val customVars: Map<String, String> = emptyMap()
) {
    override fun toString() = name
}
