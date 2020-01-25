package strongdmm.ui.search

import strongdmm.byond.dmm.MapPos

class SearchResult(
    val type: String,
    positions: List<Pair<String, MapPos>>
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SearchResult
        if (type != other.type) return false
        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}
