package strongdmm.event.type.controller

import strongdmm.event.Event

abstract class EventRecentFilesController {
    class ClearRecentEnvironments : Event<Unit, Unit>(Unit, null)
    class ClearRecentMaps : Event<Unit, Unit>(Unit, null)
}
