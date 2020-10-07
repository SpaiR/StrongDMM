package strongdmm.ui.menu_bar

import strongdmm.application.Processable
import strongdmm.application.Ui

class MenuBarUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)
    private val shortcutController = ShortcutController(viewController)

    init {
        view.viewController = viewController
    }

    override fun process() {
        view.process()
    }
}
