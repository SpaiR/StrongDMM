package strongdmm.event.service

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.MapSize
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class TriggerMapModifierService {
    class DeleteTileItemsInSelectedArea : Event<Unit, Unit>(Unit, null)
    class FillSelectedMapPositionWithTileItems(body: Array<Array<List<TileItem>>>) : Event<Array<Array<List<TileItem>>>, Unit>(body, null)

    class ReplaceTileItemsWithTypeInPositions(body: Pair<String, List<Pair<TileItem, MapPos>>>) :
        Event<Pair<String, List<Pair<TileItem, MapPos>>>, Unit>(body, null)

    class ReplaceTileItemsWithIdInPositions(body: Pair<String, List<Pair<TileItem, MapPos>>>) :
        Event<Pair<String, List<Pair<TileItem, MapPos>>>, Unit>(body, null)

    class ReplaceTileItemsByIdWithIdInPositions(body: Pair<Long, List<Pair<TileItem, MapPos>>>) :
        Event<Pair<Long, List<Pair<TileItem, MapPos>>>, Unit>(body, null)

    class DeleteTileItemsWithTypeInPositions(body: List<Pair<TileItem, MapPos>>) : Event<List<Pair<TileItem, MapPos>>, Unit>(body, null)
    class DeleteTileItemsWithIdInPositions(body: List<Pair<TileItem, MapPos>>) : Event<List<Pair<TileItem, MapPos>>, Unit>(body, null)

    class ChangeMapSize(body: MapSize) : Event<MapSize, Unit>(body, null)
}
