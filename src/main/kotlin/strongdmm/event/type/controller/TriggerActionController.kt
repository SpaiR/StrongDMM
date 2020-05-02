package strongdmm.event.type.controller

import strongdmm.byond.dmm.Dmm
import strongdmm.service.action.undoable.Undoable
import strongdmm.event.Event

abstract class TriggerActionController {
    class AddAction(body: Undoable) : Event<Undoable, Unit>(body, null)
    class UndoAction : Event<Unit, Unit>(Unit, null)
    class RedoAction : Event<Unit, Unit>(Unit, null)
    class ResetActionBalance(body: Dmm) : Event<Dmm, Unit>(body, null)
}
