package strongdmm.ui.panel.about

import strongdmm.application.Processable
import strongdmm.application.Ui

class AboutPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
