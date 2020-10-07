package strongdmm.ui.panel.coords

import strongdmm.application.Processable
import strongdmm.application.Ui

class CoordsPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
