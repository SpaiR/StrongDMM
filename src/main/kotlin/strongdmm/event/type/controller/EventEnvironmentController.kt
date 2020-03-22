package strongdmm.event.type.controller

import strongdmm.byond.dme.Dme
import strongdmm.event.EnvironmentBlockStatus
import strongdmm.event.Event
import java.io.File

abstract class EventEnvironmentController {
    class OpenEnvironment(body: File, callback: (EnvironmentBlockStatus) -> Unit) : Event<File, EnvironmentBlockStatus>(body, callback)
    class FetchOpenedEnvironment(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
}
