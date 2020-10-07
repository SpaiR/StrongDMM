package strongdmm.ui.panel.objects

import strongdmm.application.Processable
import strongdmm.application.Ui

class ObjectsPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
        eventController.viewController = viewController
    }

    override fun process() {
        view.process()
    }
}
