package strongdmm.ui.panel.coords

import strongdmm.Processable

class CoordsPanelUi : Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
