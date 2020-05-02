package strongdmm.event.type.service

import strongdmm.event.Event

abstract class TriggerRecentFilesService {
    class ClearRecentEnvironments : Event<Unit, Unit>(Unit, null)
    class ClearRecentMaps : Event<Unit, Unit>(Unit, null)
}
