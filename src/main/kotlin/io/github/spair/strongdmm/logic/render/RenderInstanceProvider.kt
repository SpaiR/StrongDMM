package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.TileItem
import io.github.spair.strongdmm.logic.map.TileItemProvider

object RenderInstanceProvider {

    fun loadMapIcons(dmm: Dmm) {
        val atoms = mutableSetOf<Int>()

        for (x in 1..dmm.maxX) {
            for (y in 1..dmm.maxY) {
                val tile = dmm.getTile(x, y)!!
                atoms.addAll(tile.getTileItemsIDs().toTypedArray())
            }
        }

        atoms.forEach {
            DmiProvider.getDmi(TileItemProvider.getByID(it).icon)?.glTextureId
        }
    }

    fun allocateRenderInstance(x: Float, y: Float, tileItem: TileItem): Long {
        val icon = tileItem.icon
        val riAddress = RenderInstanceStruct.allocate()

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

        return riAddress
    }
}
