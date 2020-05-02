package strongdmm.ui.panel.variables_preview

import strongdmm.Processable

class VariablesPreviewPanelUi : Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
