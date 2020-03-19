package strongdmm.event.type.controller

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.TileItemId
import strongdmm.event.TileItemType
import strongdmm.ui.search.SearchRect

abstract class EventInstanceController {
    class GenerateFromIconStates(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)
    class GenerateFromDirections(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)

    class FindPositionsByType(body: Pair<SearchRect, TileItemType>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<SearchRect, TileItemType>, List<Pair<TileItem, MapPos>>>(body, callback)

    class FindPositionsById(body: Pair<SearchRect, TileItemId>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<SearchRect, TileItemId>, List<Pair<TileItem, MapPos>>>(body, callback)
}
