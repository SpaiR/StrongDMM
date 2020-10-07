package strongdmm.ui.panel.variables_preview

import strongdmm.application.Processable
import strongdmm.application.Ui

class VariablesPreviewPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
