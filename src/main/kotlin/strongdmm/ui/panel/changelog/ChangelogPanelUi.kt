package strongdmm.ui.panel.changelog

import strongdmm.Processable

class ChangelogPanelUi : Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
