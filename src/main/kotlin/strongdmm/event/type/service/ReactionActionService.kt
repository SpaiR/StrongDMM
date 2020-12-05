package strongdmm.event.type.service

import strongdmm.event.Event
import strongdmm.service.action.ActionStatus

abstract class ReactionActionService {
    class ActionStatusChanged(body: ActionStatus) : Event<ActionStatus, Unit>(body, null)
}
