package strongdmm.event.type.service

import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import java.io.File

abstract class TriggerEnvironmentService {
    class OpenEnvironment(body: File, callback: ((Unit) -> Unit)? = null) : Event<File, Unit>(body, callback)
    class FetchOpenedEnvironment(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
}
