package strongdmm.ui.panel.searchresult.model

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem

class SearchResult(
    val searchObject: String, // This could be an object type or an instance ID
    val isSearchById: Boolean, // Are we using tile item ID or tile item type
    positions: List<Pair<TileItem, MapPos>>
) {
    val positions: MutableList<SearchPosition> = ArrayList(positions.size)

    init {
        var currentPos = positions[0].second
        var currentIdx = -1

        positions.forEach { (type, pos) ->
            if (currentPos == pos) {
                currentIdx++
            } else {
                currentPos = pos
                currentIdx = 0
            }

            this.positions.add(SearchPosition(type, pos))
        }
    }

    fun isEmpty(): Boolean = positions.isEmpty()
}
