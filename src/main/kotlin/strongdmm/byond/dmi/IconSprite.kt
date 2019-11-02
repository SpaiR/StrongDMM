package strongdmm.byond.dmi

class IconSprite(
    dmi: Dmi,
    index: Int
) {
    companion object {
        private const val UV_MARGIN = .000001f // This will remove gaps between separate sprites
    }

    val textureId: Int = dmi.textureId

    val iconWidth: Int = dmi.spriteWidth
    val iconHeight: Int = dmi.spriteHeight

    val u1: Float
    val v1: Float
    val u2: Float
    val v2: Float

    init {
        val x = index % dmi.cols
        val y = index / dmi.cols

        u1 = x / dmi.cols.toFloat() + UV_MARGIN
        v1 = y / dmi.rows.toFloat() + UV_MARGIN
        u2 = (x + 1) / dmi.cols.toFloat() - UV_MARGIN
        v2 = (y + 1) / dmi.rows.toFloat() - UV_MARGIN
    }
}
