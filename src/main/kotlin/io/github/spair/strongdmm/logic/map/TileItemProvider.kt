package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.EnvCleanable
import java.util.Objects

object TileItemProvider : EnvCleanable {

    private val tileItems: MutableMap<Int, TileItem> = hashMapOf()

    override fun clean() {
        tileItems.clear()
    }

    fun getOrCreate(type: String, vars: Map<String, String>?): TileItem {
        var varsAggregation: String? = null

        // Custom hashcode logic here, since default produces a lot of collisions with similar vars.
        // For example 'pixel_x = 26 pixel_y = 26' and 'pixel_x = 24 pixel_y = 24' will produce same hashcode.
        if (vars != null && vars.isNotEmpty()) {
            varsAggregation = buildString {
                vars.toSortedMap().forEach { (value, key) ->
                    append(value).append(key)
                }
            }
        }

        val hash = Objects.hash(type, varsAggregation)
        return tileItems.getOrPut(hash) { TileItem(hash, type, vars) }
    }

    fun getByID(id: Int): TileItem = tileItems.getValue(id)

    fun getByIDs(ids: IntArray): List<TileItem> {
        val tileItems = mutableListOf<TileItem>()
        ids.forEach { tileItems.add(this.tileItems.getValue(it)) }
        return tileItems
    }
}
