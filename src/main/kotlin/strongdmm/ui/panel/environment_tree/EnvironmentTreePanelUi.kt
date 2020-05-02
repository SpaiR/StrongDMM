package strongdmm.ui.panel.environment_tree

import strongdmm.Processable

class EnvironmentTreePanelUi : Processable {
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
