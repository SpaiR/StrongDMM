package strongdmm.ui.panel.searchresult.model

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem

data class SearchPosition(
    val tileItem: TileItem,
    val pos: MapPos
)
