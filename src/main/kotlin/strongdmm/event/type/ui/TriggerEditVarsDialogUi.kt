package strongdmm.event.type.ui

import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class TriggerEditVarsDialogUi {
    class OpenWithTile(tileAndTileItemIdx: Pair<Tile, Int>) : Event<Pair<Tile, Int>, Unit>(tileAndTileItemIdx, null)
    class OpenWithTileItem(body: TileItem) : Event<TileItem, Unit>(body, null)
}
