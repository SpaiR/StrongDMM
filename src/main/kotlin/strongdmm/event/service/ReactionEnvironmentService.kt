package strongdmm.event.service

import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import java.io.File

abstract class ReactionEnvironmentService {
    class EnvironmentReset private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = EnvironmentReset()
        }
    }

    class EnvironmentLoadStarted(body: File) : Event<File, Unit>(body, null)
    class EnvironmentLoadStopped(environmentLoadedStatus: Boolean) : Event<Boolean, Unit>(environmentLoadedStatus, null)
    class EnvironmentChanged(body: Dme) : Event<Dme, Unit>(body, null)
    class EnvironmentOpened : Event<Unit, Unit>(Unit, null)
}
