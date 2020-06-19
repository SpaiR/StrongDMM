package strongdmm.window

object WindowUtil {
    fun getHeightPercent(percents: Int): Float {
        return Window.windowHeight / 100f * percents
    }
}
