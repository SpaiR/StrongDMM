package strongdmm.ui.search

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem

class SearchResult(
    val searchValue: String, // This could be an object type or an instance ID
    val isSearchById: Boolean, // Do we used tile item ID or tile item type
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

            this.positions.add(SearchPosition(currentIdx, type, pos))
        }
    }
}
