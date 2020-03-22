package strongdmm.event.type.controller

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.TileItemId
import strongdmm.event.TileItemType
import strongdmm.ui.search.SearchRect

abstract class EventInstanceController {
    class GenerateInstancesFromIconStates(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)
    class GenerateInstancesFromDirections(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)

    class FindInstancePositionsByType(body: Pair<SearchRect, TileItemType>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<SearchRect, TileItemType>, List<Pair<TileItem, MapPos>>>(body, callback)

    class FindInstancePositionsById(body: Pair<SearchRect, TileItemId>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<SearchRect, TileItemId>, List<Pair<TileItem, MapPos>>>(body, callback)
}
