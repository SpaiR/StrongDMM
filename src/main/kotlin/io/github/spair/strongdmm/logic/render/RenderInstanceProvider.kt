package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.TileItem
import org.kodein.di.erased.instance
import java.util.concurrent.Executors

class RenderInstanceProvider {

    private val dmiProvider by DI.instance<DmiProvider>()
    private val placeholderTextureId = createGlTexture(DmiProvider.PLACEHOLDER_IMAGE)
    private val executor = Executors.newSingleThreadExecutor()

    private val loadedIcons = mutableSetOf<String>()
    private var locked = false

    var hasInProcessImage = false

    fun create(x: Int, y: Int, tileItem: TileItem): RenderInstance {
        val icon = tileItem.icon

        if (loadedIcons.contains(icon)) {
            hasInProcessImage = false
            return dmiProvider.getDmi(icon)?.let { dmi ->
                dmi.getIconState(tileItem.iconState)?.getIconSprite(tileItem.dir)?.let { s ->
                    val color = extractColor(tileItem)

                    RenderInstance(
                        x.toFloat() + tileItem.pixelX, y.toFloat() + tileItem.pixelY,
                        dmi.glTextureId,
                        s.u1, s.v1, s.u2, s.v2,
                        s.iconWidth.toFloat(), s.iconHeight.toFloat(),
                        color,
                        tileItem.type,
                        tileItem.plane, tileItem.layer
                    )
                }
            } ?: RenderInstance(x.toFloat(), y.toFloat(), placeholderTextureId)
        } else {
            hasInProcessImage = true

            if (!locked) {
                locked = true
                executor.execute {
                    dmiProvider.getDmi(icon) // Just to load dmi in memory
                    loadedIcons.add(icon)
                    locked = false
                }
            }

            return RenderInstance(x.toFloat(), y.toFloat(), placeholderTextureId)
        }
    }
}
