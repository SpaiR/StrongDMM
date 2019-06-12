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
    val alpha: Int = getVarInt(VAR_ALPHA).let { if (it == NON_EXISTENT_INT) 255 else it }
    val plane: Float = getVarFloat(VAR_PLANE).let { if (it == NON_EXISTENT_FLOAT) 0f else it }
    val layer: Float = getVarFloat(VAR_LAYER).let { if (it == NON_EXISTENT_FLOAT) 0f else it }
    val pixelX: Int = getVarInt(VAR_PIXEL_X).let { if (it == NON_EXISTENT_INT) 0 else it }
    val pixelY: Int = getVarInt(VAR_PIXEL_Y).let { if (it == NON_EXISTENT_INT) 0 else it }
    val dir: Int = getVarInt(VAR_DIR).let { if (it == NON_EXISTENT_INT) SOUTH else it }
    val color: String = getVarText(VAR_COLOR) ?: ""

    fun isType(otherType: String): Boolean = isType(type, otherType)

    fun getVarText(name: String): String? {
        return customVars?.get(name)?.takeIf { it.isNotEmpty() }?.run {
            substring(1, length - 1)
        } ?: dmeItem.getVarText(name)
    }

    private fun getVarInt(name: String): Int {
        return try {
            customVars?.get(name)?.toInt() ?: dmeItem.getVarInt(name)
        } catch (e: NumberFormatException) {
            NON_EXISTENT_INT
        }
    }

    private fun getVarFloat(name: String): Float {
        return try {
            customVars?.get(name)?.toFloat() ?: dmeItem.getVarFloat(name)
        } catch (e: NumberFormatException) {
            NON_EXISTENT_FLOAT
        }
    }
}
