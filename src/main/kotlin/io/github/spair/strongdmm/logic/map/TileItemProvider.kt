package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.EnvCleanable
import java.util.Objects

object TileItemProvider : EnvCleanable {

    private val tileItems: MutableMap<Int, TileItem> = hashMapOf()

    override fun clean() {
        tileItems.clear()
    }

    fun getOrCreate(type: String, vars: Map<String, String>?): TileItem {
        val hash = Objects.hash(type, vars)
        return tileItems.getOrPut(Objects.hash(type, vars)) { TileItem(hash, type, vars) }
    }

    fun getByID(id: Int): TileItem = tileItems.getValue(id)

    fun getByIDs(ids: IntArray): List<TileItem> {
        val tileItems = mutableListOf<TileItem>()
        ids.forEach { tileItems.add(this.tileItems.getValue(it)) }
        return tileItems
    }
}
