package strongdmm.ui.menu_bar

class MenuBarUi {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)
    private val shortcutController = ShortcutController(viewController)

    init {
        view.viewController = viewController
    }

    fun process() {
        view.process()
    }
}
