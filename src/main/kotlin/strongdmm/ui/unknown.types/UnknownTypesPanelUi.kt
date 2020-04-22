package strongdmm.ui.unknown.types

class UnknownTypesPanelUi {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    fun process() {
        view.process()
    }
}
