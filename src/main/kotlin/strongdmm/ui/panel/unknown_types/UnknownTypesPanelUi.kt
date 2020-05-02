package strongdmm.ui.panel.unknown_types

import strongdmm.Processable
import strongdmm.Ui

class UnknownTypesPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
