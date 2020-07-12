package strongdmm.ui.panel.coords

import strongdmm.Processable
import strongdmm.Ui

class CoordsPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
