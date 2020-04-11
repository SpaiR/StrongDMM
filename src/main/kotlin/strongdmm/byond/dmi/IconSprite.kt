package strongdmm.byond.dmi

class IconSprite(
    dmi: Dmi,
    index: Int
) {
    companion object {
        private const val UV_MARGIN = .000001f // This will remove gaps between separate sprites
    }

    val textureId: Int = dmi.textureId

    val textureWidth: Int = dmi.textureWidth
    val textureHeight: Int = dmi.textureHeight

    val iconWidth: Int = dmi.spriteWidth
    val iconHeight: Int = dmi.spriteHeight

    // Classic icon position for top-down coordinate system
    var x1: Int
    var y1: Int
    var x2: Int
    var y2: Int

    // UV mapping (used by OpenGL)
    val u1: Float
    val v1: Float
    val u2: Float
    val v2: Float

    init {
        val x = index % dmi.cols
        val y = index / dmi.cols

        x1 = x * dmi.spriteWidth
        y1 = y * dmi.spriteHeight
        x2 = (x + 1) * dmi.spriteWidth
        y2 = (y + 1) * dmi.spriteHeight

        u1 = x / dmi.cols.toFloat() + UV_MARGIN
        v1 = y / dmi.rows.toFloat() + UV_MARGIN
        u2 = (x + 1) / dmi.cols.toFloat() - UV_MARGIN
        v2 = (y + 1) / dmi.rows.toFloat() - UV_MARGIN
    }
}
