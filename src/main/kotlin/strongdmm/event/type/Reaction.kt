package strongdmm.event.type

import strongdmm.event.Event

abstract class Reaction {
    class ApplicationBlockChanged(applicationBlockStatus: Boolean) : Event<Boolean, Unit>(applicationBlockStatus, null)
}
