package strongdmm.ui.panel.instance_locator

class InstanceLocatorPanelUi {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
        eventController.viewController = viewController
    }

    fun postInit() {
        eventController.postInit()
    }

    fun process() {
        view.process()
    }
}
