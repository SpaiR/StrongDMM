package strongdmm.ui.tile_popup

class TilePopupUi {
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
