package strongdmm.ui.unknown_types_panel

class UnknownTypesPanelUi {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    fun process() {
        view.process()
    }
}
