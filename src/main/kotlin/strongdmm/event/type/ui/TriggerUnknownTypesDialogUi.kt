package strongdmm.event.type.ui

import strongdmm.event.Event
import strongdmm.service.map.UnknownType

abstract class TriggerUnknownTypesDialogUi {
    class Open(body: Set<UnknownType>, callback: (Unit) -> Unit) : Event<Set<UnknownType>, Unit>(body, callback)
}
