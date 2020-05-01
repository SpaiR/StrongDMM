package strongdmm.ui.panel.search_result

class SearchResultPanelUi {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
        eventController.viewController = viewController
    }

    fun process() {
        view.process()
    }
}
