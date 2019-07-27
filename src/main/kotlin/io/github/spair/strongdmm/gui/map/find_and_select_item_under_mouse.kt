package io.github.spair.strongdmm.gui.map

import gnu.trove.list.array.TLongArrayList
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.TileItem
import io.github.spair.strongdmm.logic.map.TileItemProvider
import io.github.spair.strongdmm.logic.render.RenderInstanceStruct

fun MapPipeline.findAndSelectItemUnderMouse(renderInstances: LongArray) {
    val addresses = TLongArrayList()

    renderInstances.forEach { riAddress ->
        val locX = RenderInstanceStruct.getLocX(riAddress)
        val locY = RenderInstanceStruct.getLocY(riAddress)
        val width = RenderInstanceStruct.getWidth(riAddress)
        val height = RenderInstanceStruct.getHeight(riAddress)

        if (xMouse in locX..(locX + width) && yMouse in locY..(locY + height)) {
            addresses.add(riAddress)
        }
    }

    var selectedItem: TileItem? = null

    for (riAddress in addresses) {
        val locX = RenderInstanceStruct.getLocX(riAddress)
        val locY = RenderInstanceStruct.getLocY(riAddress)
        val width = RenderInstanceStruct.getWidth(riAddress)
        val tileItemID = RenderInstanceStruct.getTileItemId(riAddress)

        val pixelX = (xMouse - locX).toInt()
        val pixelY = (width - (yMouse - locY)).toInt()

        val item = TileItemProvider.getByID(tileItemID)
        val isOpaque = DmiProvider.getSpriteFromDmi(item.icon, item.iconState, item.dir)?.isOpaquePixel(pixelX, pixelY)
            ?: true // When there is no sprite for item we are using placeholder which is always opaque

        if (isOpaque) {
            selectedItem = item
        }
    }

    selectedItem?.let {
        ObjectTreeView.findAndSelectItemInstance(it)
    }
}
