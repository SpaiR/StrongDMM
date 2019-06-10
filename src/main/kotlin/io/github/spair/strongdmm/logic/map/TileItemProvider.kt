package io.github.spair.strongdmm.logic.map

import java.util.Objects

object TileItemProvider {

    private val tileItems: MutableMap<Int, TileItem> = hashMapOf()

    fun getOrCreate(type: String, vars: Map<String, String>?): TileItem {
        return tileItems.computeIfAbsent(Objects.hash(type, vars)) { TileItem(it, type, vars) }
    }

    fun getByID(id: Int): TileItem = tileItems.getValue(id)

    fun getByIDs(ids: List<Int>): List<TileItem> {
        val tileItems = mutableListOf<TileItem>()
        ids.forEach { tileItems.add(this.tileItems.getValue(it)) }
        return tileItems
    }
}
