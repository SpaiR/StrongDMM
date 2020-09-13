package strongdmm.event.type.service

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class TriggerInstanceService {
    class GenerateInstancesFromIconStates(body: TileItem) : Event<TileItem, Unit>(body, null)
    class GenerateInstancesFromDirections(body: TileItem) : Event<TileItem, Unit>(body, null)

    class FindInstancePositionsByType(body: Pair<MapArea, String>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<MapArea, String>, List<Pair<TileItem, MapPos>>>(body, callback)

    class FindInstancePositionsById(body: Pair<MapArea, Long>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) :
        Event<Pair<MapArea, Long>, List<Pair<TileItem, MapPos>>>(body, callback)

    class EditInstance(body: TileItem) : Event<TileItem, Unit>(body, null)
    class DeleteInstance(body: TileItem) : Event<TileItem, Unit>(body, null)
}
