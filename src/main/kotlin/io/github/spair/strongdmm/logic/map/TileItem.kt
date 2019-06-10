package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.dmi.SOUTH
import java.util.Collections

class TileItem(val id: Int, val type: String, customVars: Map<String, String>? = null) {

    val dmeItem: DmeItem get() = Environment.dme.getItem(type)!!
    val customVars: Map<String, String>? = customVars?.takeIf { it.isNotEmpty() }?.let { Collections.unmodifiableMap(it) }

    // Vars extensively used during rendering
    val icon: String = getVarText(VAR_ICON) ?: ""
    val iconState: String = getVarText(VAR_ICON_STATE) ?: ""
    val alpha: Int = getVarInt(VAR_ALPHA) ?: 255
    val plane: Float = getVarFloat(VAR_PLANE) ?: 0f
    val layer: Float = getVarFloat(VAR_LAYER) ?: 0f
    val pixelX: Int = getVarInt(VAR_PIXEL_X) ?: 0
    val pixelY: Int = getVarInt(VAR_PIXEL_Y) ?: 0
    val dir: Int = getVarInt(VAR_DIR) ?: SOUTH
    val color: String = getVarText(VAR_COLOR) ?: ""

    fun isType(otherType: String): Boolean = isType(type, otherType)

    fun getVar(name: String): String? = customVars?.get(name) ?: dmeItem.getVar(name)
    fun getVarText(name: String): String? = customVars?.get(name)?.takeIf { it.isNotEmpty() }?.run { substring(1, length - 1) } ?: dmeItem.getVarText(name)
    fun getVarInt(name: String): Int? = customVars?.get(name)?.toIntOrNull() ?: dmeItem.getVarInt(name)
    fun getVarFloat(name: String): Float? = customVars?.get(name)?.toFloatOrNull() ?: dmeItem.getVarFloat(name)
}