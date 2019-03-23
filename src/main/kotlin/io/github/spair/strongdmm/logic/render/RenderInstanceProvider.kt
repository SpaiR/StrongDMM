package io.github.spair.strongdmm.logic.render

import io.github.spair.byond.ByondVars
import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.dmi.SOUTH
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
        val icon = tileItem.getVarPath(ByondVars.ICON) ?: ""

        if (loadedIcons.contains(icon)) {
            hasInProcessImage = false
            return dmiProvider.getDmi(icon)?.let { dmi ->

                val iconState = tileItem.getVarText(ByondVars.ICON_STATE) ?: ""
                val dir = tileItem.getVarInt(ByondVars.DIR) ?: SOUTH

                dmi.getIconState(iconState)?.getIconSprite(dir)?.let { s ->
                    val color = extractColor(tileItem)
                    val pixelX = tileItem.getVarInt(ByondVars.PIXEL_X) ?: 0
                    val pixelY = tileItem.getVarInt(ByondVars.PIXEL_Y) ?: 0
                    val plane = tileItem.getVarDouble(ByondVars.PLANE)?.toFloat() ?: 0f
                    val layer = tileItem.getVarDouble(ByondVars.LAYER)?.toFloat() ?: 0f

                    RenderInstance(
                        x.toFloat() + pixelX, y.toFloat() + pixelY,
                        dmi.glTextureId,
                        s.u1, s.v1, s.u2, s.v2,
                        s.iconWidth.toFloat(), s.iconHeight.toFloat(),
                        color,
                        tileItem.type,
                        plane, layer
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
