package strongdmm.event.type.service

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class TriggerInstanceService {
    class GenerateInstancesFromIconStates(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)
    class GenerateInstancesFromDirections(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)

    class FindInstancePositionsByType(body: Pair<MapArea, String>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<MapArea, String>, List<Pair<TileItem, MapPos>>>(body, callback)

    class FindInstancePositionsById(body: Pair<MapArea, Long>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<MapArea, Long>, List<Pair<TileItem, MapPos>>>(body, callback)
}
