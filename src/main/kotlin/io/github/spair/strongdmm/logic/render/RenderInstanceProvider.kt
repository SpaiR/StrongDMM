package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.logic.EnvCleanable
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.TileItem
import java.util.concurrent.Executors

object RenderInstanceProvider : EnvCleanable {

    private var executor = Executors.newSingleThreadExecutor()
    private var locked = false

    var hasInProcessImage = false

    override fun clean() {
        executor.shutdownNow()
        executor = Executors.newSingleThreadExecutor()
        locked = false
    }

    fun create(x: Float, y: Float, tileItem: TileItem): RenderInstance {
        val icon = tileItem.icon

        if (DmiProvider.hasDmiInMemory(icon)) {
            hasInProcessImage = false
            return DmiProvider.getDmi(icon)?.let { dmi ->
                dmi.getIconState(tileItem.iconState)?.getIconSprite(tileItem.dir)?.let { s ->
                    RenderInstance(
                        x + tileItem.pixelX, y + tileItem.pixelY,
                        dmi.glTextureId,
                        s.u1, s.v1, s.u2, s.v2,
                        s.iconWidth, s.iconHeight,
                        ColorExtractor.extractColor(tileItem),
                        tileItem
                    )
                }
            } ?: RenderInstance(x, y, DmiProvider.placeholderTextureId, tileItem = tileItem)
        } else {
            hasInProcessImage = true

            if (!locked) {
                locked = true
                executor.execute {
                    DmiProvider.getDmi(icon) // Just to load dmi in memory
                    locked = false
                }
            }

            return RenderInstance(x, y, DmiProvider.placeholderTextureId, tileItem = tileItem)
        }
    }
}
