package strongdmm.ui.about.panel

class AboutPanelUi {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    fun process() {
        view.process()
    }
}
