package strongdmm.event.type.controller

import strongdmm.controller.action.undoable.Undoable
import strongdmm.event.Event

abstract class EventActionController {
    class AddAction(body: Undoable) : Event<Undoable, Unit>(body, null)
    class UndoAction : Event<Unit, Unit>(Unit, null)
    class RedoAction : Event<Unit, Unit>(Unit, null)
    class ResetActionBalance : Event<Unit, Unit>(Unit, null)
}
