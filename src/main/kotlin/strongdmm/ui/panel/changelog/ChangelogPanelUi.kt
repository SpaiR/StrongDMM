package strongdmm.ui.panel.changelog

import strongdmm.Processable
import strongdmm.Ui

class ChangelogPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
