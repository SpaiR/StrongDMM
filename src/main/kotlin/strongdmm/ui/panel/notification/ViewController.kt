package strongdmm.ui.panel.notification

class ViewController(
    private val state: State
) {
    companion object {
        private const val WINDOW_MOD_STEP: Float = 5f
        private const val HEIGHT: Float = 30f
    }

    fun isOpening(): Boolean {
        if (state.notificationText != null && state.windowHeight < HEIGHT) {
            state.windowHeight += WINDOW_MOD_STEP
            return true
        }
        return false
    }

    fun isClosing(): Boolean {
        if (state.notificationText == null && state.windowHeight > 0) {
            state.windowHeight -= WINDOW_MOD_STEP
            return true
        }
        return false
    }
}
