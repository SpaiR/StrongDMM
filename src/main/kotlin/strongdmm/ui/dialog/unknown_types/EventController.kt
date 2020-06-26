package strongdmm.ui.dialog.unknown_types

import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerUnknownTypesDialogUi
import strongdmm.service.map.UnknownType

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerUnknownTypesDialogUi.Open::class.java, ::handleOpen)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
    }

    private fun handleOpen(event: Event<Set<UnknownType>, Unit>) {
        state.unknownTypes = event.body
        state.eventToReply = event
        state.isDoOpen = true
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        state.currentEnvironment = event.body
    }
}
