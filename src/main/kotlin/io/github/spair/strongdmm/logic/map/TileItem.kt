package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.gui.instancelist.ItemInstance
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.dmi.SOUTH

class TileItem(val type: String, var x: Int, var y: Int, customVars: Map<String, String>? = null) {

    val dmeItem: DmeItem get() = Environment.dme.getItem(type)!!
    val customVars: Map<String, String>?
        get() = customVarsBacked

    private var customVarsBacked: MutableMap<String, String>? = customVars?.takeIf { it.isNotEmpty() }?.let { HashMap(it) }

    companion object {
        fun fromInstance(instance: ItemInstance, x: Int, y: Int): TileItem {
            return TileItem(
                Environment.dme.getItem(
                    instance.type
                )!!.type, x, y, instance.customVars
            )
        }
        fun fromTileItem(tileItem: TileItem, x: Int, y: Int): TileItem {
            return TileItem(tileItem.type, x, y, tileItem.customVars)
        }
    }

    // Vars extensively used during rendering
    var icon = getVarText(VAR_ICON) ?: ""
    var iconState = getVarText(VAR_ICON_STATE) ?: ""
    var alpha = getVarInt(VAR_ALPHA) ?: 255
    var plane = getVarFloat(VAR_PLANE) ?: 0f
    var layer = getVarFloat(VAR_LAYER) ?: 0f
    var pixelX = getVarInt(VAR_PIXEL_X) ?: 0
    var pixelY = getVarInt(VAR_PIXEL_Y) ?: 0
    var dir = getVarInt(VAR_DIR) ?: SOUTH
    var color = getVarText(VAR_COLOR) ?: ""

    fun updateFields() {
        icon = getVarText(VAR_ICON) ?: ""
        iconState = getVarText(VAR_ICON_STATE) ?: ""
        alpha = getVarInt(VAR_ALPHA) ?: 255
        plane = getVarFloat(VAR_PLANE) ?: 0f
        layer = getVarFloat(VAR_LAYER) ?: 0f
        pixelX = getVarInt(VAR_PIXEL_X) ?: 0
        pixelY = getVarInt(VAR_PIXEL_Y) ?: 0
        dir = getVarInt(VAR_DIR) ?: SOUTH
        color = getVarText(VAR_COLOR) ?: ""
    }

    fun addVar(name: String, value: String) {
        if (customVarsBacked == null) {
            customVarsBacked = HashMap()
        }
        customVarsBacked!![name] = value
    }

    fun removeVar(name: String) {
        customVarsBacked?.let { vars ->
            vars.remove(name)
            if (vars.isEmpty()) {
                customVarsBacked = null
            }
        }
    }

    fun reset() {
        customVarsBacked?.clear()
        updateFields()
    }

    fun resetWithVars(newVars: Map<String, String>?) {
        customVarsBacked = newVars?.takeIf { it.isNotEmpty() }?.let { HashMap(it) }
        updateFields()
    }

    fun isType(otherType: String) = isType(type, otherType)

    fun getVar(name: String): String? = customVarsBacked?.get(name) ?: dmeItem.getVar(name)
    fun getVarText(name: String): String? = customVarsBacked?.get(name)?.takeIf { it.isNotEmpty() }?.run { substring(1, length - 1) } ?: dmeItem.getVarText(name)
    fun getVarInt(name: String): Int? = customVarsBacked?.get(name)?.toIntOrNull() ?: dmeItem.getVarInt(name)
    fun getVarFloat(name: String): Float? = customVarsBacked?.get(name)?.toFloatOrNull() ?: dmeItem.getVarFloat(name)
}