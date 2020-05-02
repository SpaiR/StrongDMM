package strongdmm.ui.panel.unknown_types

import strongdmm.Processable

class UnknownTypesPanelUi : Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
