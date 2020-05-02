package strongdmm.event.type.controller

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import java.io.File

abstract class TriggerMapHolderController {
    class CreateNewMap(body: File) : Event<File, Unit>(body, null)
    class OpenMap(body: File) : Event<File, Unit>(body, null)
    class CloseMap(mapId: Int) : Event<Int, Unit>(mapId, null)
    class CloseSelectedMap : Event<Unit, Unit>(Unit, null)
    class CloseAllMaps(callback: ((Boolean) -> Unit)? = null) : Event<Unit, Boolean>(Unit, callback)
    class FetchSelectedMap(callback: ((Dmm) -> Unit)) : Event<Unit, Dmm>(Unit, callback)
    class ChangeSelectedMap(mapId: Int) : Event<Int, Unit>(mapId, null)
    class SaveSelectedMap : Event<Unit, Unit>(Unit, null)
    class SaveSelectedMapToFile(body: File) : Event<File, Unit>(body, null)
    class SaveAllMaps : Event<Unit, Unit>(Unit, null)
    class ChangeSelectedZ(zSelected: Int) : Event<Int, Unit>(zSelected, null)
}
