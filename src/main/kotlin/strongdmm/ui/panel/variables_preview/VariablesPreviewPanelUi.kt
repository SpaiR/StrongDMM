package strongdmm.ui.panel.variables_preview

class VariablesPreviewPanelUi {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    fun process() {
        view.process()
    }
}
