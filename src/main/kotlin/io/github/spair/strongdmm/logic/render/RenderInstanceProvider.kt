package io.github.spair.strongdmm.logic.render

import io.github.spair.byond.ByondVars
import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.dmi.SOUTH
import io.github.spair.strongdmm.logic.map.TileItem
import org.kodein.di.erased.instance

class RenderInstanceProvider {

    private val dmiProvider by DI.instance<DmiProvider>()
    private val placeholderTextureId by lazy { createGlTexture(DmiProvider.PLACEHOLDER_IMAGE) }

    fun create(x: Int, y: Int, tileItem: TileItem): RenderInstance {
        val icon = tileItem.getVarFilePathSafe(ByondVars.ICON).orElse("")
        val iconState = tileItem.getVarTextSafe(ByondVars.ICON_STATE).orElse("")
        val dir = tileItem.getVarIntSafe(ByondVars.DIR).orElse(SOUTH)

        return dmiProvider.getDmi(icon)?.let { dmi ->
            dmi.getIconState(iconState)?.getIconSprite(dir)?.let { s ->
                RenderInstance(
                    x.toFloat(), y.toFloat(),
                    dmi.glTextureId,
                    s.u1, s.v1, s.u2, s.v2,
                    s.iconWidth.toFloat(), s.iconHeight.toFloat()
                )
            }
        } ?: RenderInstance(x.toFloat(), y.toFloat(), placeholderTextureId)
    }
}
