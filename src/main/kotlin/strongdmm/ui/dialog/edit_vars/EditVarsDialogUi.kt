package strongdmm.ui.dialog.edit_vars

import strongdmm.Processable

class EditVarsDialogUi : Processable {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
        eventController.viewController = viewController
    }

    override fun process() {
        view.process()
    }
}
