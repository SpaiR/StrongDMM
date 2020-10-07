package strongdmm.ui.panel.opened_maps

import strongdmm.application.Processable
import strongdmm.application.Ui

class OpenedMapsPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
    }

    override fun process() {
        view.process()
    }
}
