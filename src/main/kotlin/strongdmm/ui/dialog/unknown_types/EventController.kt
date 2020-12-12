package strongdmm.ui.dialog.unknown_types

import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ReactionEnvironmentService
import strongdmm.event.ui.TriggerUnknownTypesDialogUi
import strongdmm.service.map.UnknownType

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(TriggerUnknownTypesDialogUi.Open::class.java, ::handleOpen)
        EventBus.sign(ReactionEnvironmentService.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
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
