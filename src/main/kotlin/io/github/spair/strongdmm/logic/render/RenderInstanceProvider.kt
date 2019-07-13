package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.common.TYPE_AREA
import io.github.spair.strongdmm.common.TYPE_MOB
import io.github.spair.strongdmm.common.TYPE_OBJ
import io.github.spair.strongdmm.common.TYPE_TURF
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.TileItem
import io.github.spair.strongdmm.logic.map.TileItemProvider
import org.lwjgl.opengl.GL11

object RenderInstanceProvider {

    private val renderDataCache: MutableMap<String, RenderData?> = hashMapOf()

    fun clearTextures() {
        renderDataCache.values.forEach { data -> data?.let { GL11.glDeleteTextures(it.glTextureId) } }
        renderDataCache.clear()
    }

    fun loadMapIcons(dmm: Dmm) {
        val area = mutableSetOf<Int>()
        val turf = mutableSetOf<Int>()
        val objs = mutableSetOf<Int>()
        val mobs = mutableSetOf<Int>()

        for (x in 1..dmm.getMaxX()) {
            for (y in 1..dmm.getMaxY()) {
                val tile = dmm.getTile(x, y)!!
                area.addAll(tile.getAllTileItemsIsType(TYPE_AREA).map { it.id })
                turf.addAll(tile.getAllTileItemsIsType(TYPE_TURF).map { it.id })
                objs.addAll(tile.getAllTileItemsIsType(TYPE_OBJ).map { it.id })
                mobs.addAll(tile.getAllTileItemsIsType(TYPE_MOB).map { it.id })
            }
        }

        arrayOf(area, turf, objs, mobs).forEach { atoms ->
            atoms.forEach {
                cacheRenderData(TileItemProvider.getByID(it))
            }
        }

        DmiProvider.cleanCache()
        System.gc()
    }

    fun allocateRenderInstance(x: Float, y: Float, tileItem: TileItem): Long {
        val riAddress = RenderInstanceStruct.allocate()
        val key = tileItem.icon + tileItem.iconState + tileItem.dir

        if (!renderDataCache.containsKey(key)) {
            cacheRenderData(tileItem)
        }

        val renderData = renderDataCache[key]

        if (renderData != null) {
            RenderInstanceStruct.setMajor(
                riAddress,
                x + tileItem.pixelX, y + tileItem.pixelY,
                renderData.glTextureId,
                renderData.u1, renderData.v1, renderData.u2, renderData.v2,
                renderData.iconWidth, renderData.iconHeight,
                tileItem.id
            )

            ColorExtractor.extractAndSetColor(riAddress, tileItem)
        } else {
            RenderInstanceStruct.setMajor(riAddress, x, y, DmiProvider.placeholderTextureId, tileItemID = tileItem.id)
            RenderInstanceStruct.setColor(riAddress)
        }

        return riAddress
    }

    private fun cacheRenderData(tileItem: TileItem) {
        val dmi = DmiProvider.getDmi(tileItem.icon)
        val sprite = dmi?.getIconState(tileItem.iconState)?.getIconSprite(tileItem.dir)
        val key = tileItem.icon + tileItem.iconState + tileItem.dir

        if (sprite != null) {
            renderDataCache[key] = RenderData(
                dmi.glTextureId, sprite.u1, sprite.v1, sprite.u2, sprite.v2, sprite.iconWidth, sprite.iconHeight
            )
        } else {
            renderDataCache[key] = null
        }
    }

    private class RenderData(
        val glTextureId: Int,
        val u1: Float,
        val v1: Float,
        val u2: Float,
        val v2: Float,
        val iconWidth: Int,
        val iconHeight: Int
    )
}
