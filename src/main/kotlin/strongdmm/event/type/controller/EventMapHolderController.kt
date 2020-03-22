package strongdmm.event.type.controller

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.MapId
import java.io.File

abstract class EventMapHolderController {
    class OpenMap(body: File) : Event<File, Unit>(body, null)
    class CloseMap(body: MapId) : Event<MapId, Unit>(body, null)
    class FetchSelectedMap(callback: ((Dmm) -> Unit)) : Event<Unit, Dmm>(Unit, callback)
    class ChangeSelectedMap(body: MapId) : Event<MapId, Unit>(body, null)
    class SaveSelectedMap : Event<Unit, Unit>(Unit, null)
}
