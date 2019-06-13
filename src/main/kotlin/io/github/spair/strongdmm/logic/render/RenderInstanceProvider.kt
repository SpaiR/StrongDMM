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

    fun allocateRenderInstance(x: Float, y: Float, tileItem: TileItem): Long {
        val icon = tileItem.icon
        val riAddress = RenderInstanceStruct.allocate()

        if (DmiProvider.hasDmiInMemory(icon)) {
            hasInProcessImage = false

            val dmi = DmiProvider.getDmi(icon)
            val sprite = dmi?.getIconState(tileItem.iconState)?.getIconSprite(tileItem.dir)

            if (sprite != null) {
                RenderInstanceStruct.setMajor(
                    riAddress,
                    x + tileItem.pixelX, y + tileItem.pixelY,
                    dmi.glTextureId,
                    sprite.u1, sprite.v1, sprite.u2, sprite.v2,
                    sprite.iconWidth, sprite.iconHeight,
                    tileItem.id
                )

                ColorExtractor.extractAndSetColor(riAddress, tileItem)
            } else {
                RenderInstanceStruct.setMajor(riAddress, x, y, DmiProvider.placeholderTextureId, tileItemID = tileItem.id)
                RenderInstanceStruct.setColor(riAddress)
            }
        } else {
            hasInProcessImage = true

            if (!locked) {
                locked = true
                executor.execute {
                    DmiProvider.getDmi(icon) // Just to load dmi in memory
                    locked = false
                }
            }

            RenderInstanceStruct.setMajor(riAddress, x, y, DmiProvider.placeholderTextureId, tileItemID = tileItem.id)
            RenderInstanceStruct.setColor(riAddress)
        }

        return riAddress
    }
}
