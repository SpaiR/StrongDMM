package strongdmm.byond.dmm

import strongdmm.byond.*
import strongdmm.byond.dme.DmeItem
import java.util.Collections

class TileItem(
    val id: Int,
    val dmeItem: DmeItem,
    customVars: Map<String, String>?
) {
    val customVars: Map<String, String>? = customVars?.takeIf { it.isNotEmpty() }?.let { Collections.unmodifiableMap(it) }
    val type: String get() = dmeItem.type

    // Props extensively used during rendering
    val icon: String get() = getVarText(VAR_ICON) ?: ""
    val iconState: String get() = getVarText(VAR_ICON_STATE) ?: ""
    val alpha: Int get() = getVarInt(VAR_ALPHA) ?: 0
    val plane: Float get() = getVarFloat(VAR_PLANE) ?: 0f
    val layer: Float get() = getVarFloat(VAR_LAYER) ?: 0f
    val pixelX: Int get() = getVarInt(VAR_PIXEL_X) ?: 0
    val pixelY: Int get() = getVarInt(VAR_PIXEL_Y) ?: 0
    val dir: Int get() = getVarInt(VAR_DIR) ?: 0
    val color: String get() = getVarText(VAR_COLOR) ?: ""

    fun getVarText(name: String): String? {
        return customVars?.get(name)?.takeIf { it.isNotEmpty() }?.run {
            substring(1, length - 1)
        } ?: dmeItem.getVarText(name)
    }

    fun getVarInt(name: String): Int? = customVars?.get(name)?.toIntOrNull() ?: dmeItem.getVarInt(name)

    fun getVarFloat(name: String): Float? = customVars?.get(name)?.toFloatOrNull() ?: dmeItem.getVarFloat(name)
}
