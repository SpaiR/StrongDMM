package strongdmm.event.type.controller

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.TileItemId
import strongdmm.event.TileItemType

abstract class TriggerInstanceController {
    class GenerateInstancesFromIconStates(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)
    class GenerateInstancesFromDirections(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)

    class FindInstancePositionsByType(body: Pair<MapArea, TileItemType>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<MapArea, TileItemType>, List<Pair<TileItem, MapPos>>>(body, callback)

    class FindInstancePositionsById(body: Pair<MapArea, TileItemId>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<MapArea, TileItemId>, List<Pair<TileItem, MapPos>>>(body, callback)
}
