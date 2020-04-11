package strongdmm.event.type.controller

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.MapSize
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.TileItemType

abstract class TriggerMapModifierController {
    class DeleteTileItemsInActiveArea : Event<Unit, Unit>(Unit, null)
    class FillSelectedMapPositionWithTileItems(body: Array<Array<List<TileItem>>>) : Event<Array<Array<List<TileItem>>>, Unit>(body, null)

    class ReplaceTileItemsWithTypeInPositions(body: Pair<TileItemType, List<Pair<TileItem, MapPos>>>) :
        Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>(body, null)

    class ReplaceTileItemsWithIdInPositions(body: Pair<TileItemType, List<Pair<TileItem, MapPos>>>) :
        Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>(body, null)

    class DeleteTileItemsWithTypeInPositions(body: List<Pair<TileItem, MapPos>>) : Event<List<Pair<TileItem, MapPos>>, Unit>(body, null)
    class DeleteTileItemsWithIdInPositions(body: List<Pair<TileItem, MapPos>>) : Event<List<Pair<TileItem, MapPos>>, Unit>(body, null)

    class ChangeMapSize(body: MapSize) : Event<MapSize, Unit>(body, null)
}
