package strongdmm.ui.panel.variables_preview

import strongdmm.Processable
import strongdmm.Ui

class VariablesPreviewPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
