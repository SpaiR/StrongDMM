package strongdmm.byond.dmm

import gnu.trove.map.hash.TIntObjectHashMap
import strongdmm.byond.dme.Dme
import strongdmm.util.extension.getOrPut
import java.util.Objects

object GlobalTileItemHolder {
    lateinit var environment: Dme

    private val tileItems: TIntObjectHashMap<TileItem> = TIntObjectHashMap()

    fun resetEnvironment() {
        tileItems.clear()
    }

    fun getOrCreate(type: String, vars: Map<String, String>?): TileItem {
        var varsAggregation: String? = null

        // Custom hashcode logic here, since the default one will produce a lot of collisions with similar variables.
        // For example 'pixel_x = 26 pixel_y = 26' and 'pixel_x = 24 pixel_y = 24' will produce the same hashcode.
        if (vars != null && vars.isNotEmpty()) {
            varsAggregation = buildString {
                vars.toSortedMap().forEach { (value, key) ->
                    append(value).append(key)
                }
            }
        }

        val hash = Objects.hash(type, varsAggregation)
        return tileItems.getOrPut(hash) { TileItem(hash, environment.getItem(type)!!, vars) }
    }

    fun getById(id: Int): TileItem = tileItems[id]
}
