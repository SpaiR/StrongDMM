package strongdmm.event.type.controller

import strongdmm.byond.dmm.Dmm
import strongdmm.event.ActiveZ
import strongdmm.event.Event
import strongdmm.event.MapId
import strongdmm.event.MapsCloseStatus
import java.io.File

abstract class TriggerMapHolderController {
    class OpenMap(body: File) : Event<File, Unit>(body, null)
    class CloseMap(body: MapId) : Event<MapId, Unit>(body, null)
    class CloseSelectedMap : Event<Unit, Unit>(Unit, null)
    class CloseAllMaps(callback: ((MapsCloseStatus) -> Unit)? = null) : Event<Unit, MapsCloseStatus>(Unit, callback)
    class FetchSelectedMap(callback: ((Dmm) -> Unit)) : Event<Unit, Dmm>(Unit, callback)
    class ChangeSelectedMap(body: MapId) : Event<MapId, Unit>(body, null)
    class SaveSelectedMap : Event<Unit, Unit>(Unit, null)
    class SaveAllMaps : Event<Unit, Unit>(Unit, null)
    class ChangeActiveZ(body: ActiveZ) : Event<ActiveZ, Unit>(body, null)
}
