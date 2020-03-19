package strongdmm.event.type.controller

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.MapId
import java.io.File

abstract class EventMapHolderController {
    class Open(body: File) : Event<File, Unit>(body, null)
    class Close(body: MapId) : Event<MapId, Unit>(body, null)
    class FetchSelected(callback: ((Dmm) -> Unit)) : Event<Unit, Dmm>(Unit, callback)
    class Switch(body: MapId) : Event<MapId, Unit>(body, null)
    class Save : Event<Unit, Unit>(Unit, null)
}
