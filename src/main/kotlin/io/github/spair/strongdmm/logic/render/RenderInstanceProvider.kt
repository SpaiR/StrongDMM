package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.TileItem
import java.util.concurrent.Executors

class RenderInstanceProvider {

    private val dmiProvider by diInstance<DmiProvider>()
    private val placeholderTextureId = createGlTexture(DmiProvider.PLACEHOLDER_IMAGE)
    private val executor = Executors.newSingleThreadExecutor()

    private var locked = false

    var hasInProcessImage = false

    fun create(x: Float, y: Float, tileItem: TileItem): RenderInstance {
        val icon = tileItem.icon

        if (dmiProvider.hasDmiInMemory(icon)) {
            hasInProcessImage = false
            return dmiProvider.getDmi(icon)?.let { dmi ->
                dmi.getIconState(tileItem.iconState)?.getIconSprite(tileItem.dir)?.let { s ->
                    RenderInstance(
                        x + tileItem.pixelX, y + tileItem.pixelY,
                        dmi.glTextureId,
                        s.u1, s.v1, s.u2, s.v2,
                        s.iconWidth, s.iconHeight,
                        extractColor(tileItem),
                        tileItem.type,
                        tileItem.plane, tileItem.layer
                    )
                }
            } ?: RenderInstance(x, y, placeholderTextureId)
        } else {
            hasInProcessImage = true

            if (!locked) {
                locked = true
                executor.execute {
                    dmiProvider.getDmi(icon) // Just to load dmi in memory
                    locked = false
                }
            }

            return RenderInstance(x, y, placeholderTextureId)
        }
    }
}
