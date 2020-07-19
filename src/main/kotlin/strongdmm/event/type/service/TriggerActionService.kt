package strongdmm.event.type.service

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.service.action.undoable.Undoable

abstract class TriggerActionService {
    class BatchActions(scope: ((() -> Unit)) -> Unit) : Event<((() -> Unit)) -> Unit, Unit>(scope, null)
    class AddAction(body: Undoable) : Event<Undoable, Unit>(body, null)
    class UndoAction : Event<Unit, Unit>(Unit, null)
    class RedoAction : Event<Unit, Unit>(Unit, null)
    class ResetActionBalance(body: Dmm) : Event<Dmm, Unit>(body, null)
}
