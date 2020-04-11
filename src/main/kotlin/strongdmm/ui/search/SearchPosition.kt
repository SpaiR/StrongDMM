package strongdmm.ui.search

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem

data class SearchPosition(
    val idx: Int,
    val tileItem: TileItem,
    val pos: MapPos
)
