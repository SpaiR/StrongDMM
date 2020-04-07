package strongdmm.event.type.controller

import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import java.io.File

abstract class TriggerEnvironmentController {
    class OpenEnvironment(body: File) : Event<File, Unit>(body, null)
    class FetchOpenedEnvironment(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
}
