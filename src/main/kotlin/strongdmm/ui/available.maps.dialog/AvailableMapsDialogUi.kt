package strongdmm.ui.available.maps.dialog

class AvailableMapsDialogUi {
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
