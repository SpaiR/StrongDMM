package strongdmm.ui.panel.about

import strongdmm.Processable

class AboutPanelUi : Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
