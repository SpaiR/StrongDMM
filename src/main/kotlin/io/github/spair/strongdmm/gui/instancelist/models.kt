package io.github.spair.strongdmm.gui.instancelist

data class ItemInstance(
    val name: String,
    val icon: String,
    val iconState: String,
    val type: String,
    val dir: Int,
    val customVars: Map<String, String>?
) {
    override fun toString() = name
}
