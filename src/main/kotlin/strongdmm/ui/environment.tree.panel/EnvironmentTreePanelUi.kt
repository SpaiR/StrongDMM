package strongdmm.ui.environment.tree.panel

class EnvironmentTreePanelUi {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
    }

    fun process() {
        view.process()
    }
}
