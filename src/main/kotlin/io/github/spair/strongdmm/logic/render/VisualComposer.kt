package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.logic.dme.TYPE_AREA
import io.github.spair.strongdmm.logic.dmi.EAST
import io.github.spair.strongdmm.logic.dmi.NORTH
import io.github.spair.strongdmm.logic.dmi.SOUTH
import io.github.spair.strongdmm.logic.dmi.WEST
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.LayersManager
import io.github.spair.strongdmm.logic.map.TileItemProvider
import java.util.TreeMap

typealias RenderInstances = TreeMap<Float, TreeMap<Float, MutableList<Long>>>

private fun RenderInstances.get(plane: Float, layer: Float): MutableList<Long> {
    return computeIfAbsent(plane) { TreeMap() }.computeIfAbsent(layer) { mutableListOf() }
}

object VisualComposer {

    // We will look for things outside of viewport range to handle big objects (bigger then /world/icon_size)
    private const val ADDITIONAL_VIEW_RANGE = 2

    private var xMapOffPrev: Int = 0
    private var yMapOffPrev: Int = 0
    private var horTilesNumPrev: Int = 0
    private var verTilesNumPrev: Int = 0
    private var drawAreasBorderPrev: Boolean = true

    private var riCache: RenderInstances? = null

    val framedAreas: MutableList<FramedArea> = mutableListOf()

    fun clearCache() {
        deallocateCache()
        framedAreas.clear()
        riCache = null
    }

    fun composeFrame(
        dmm: Dmm,
        xMapOff: Int,
        yMapOff: Int,
        horTilesNum: Int,
        verTilesNum: Int,
        forceUpdate: Boolean,
        drawAreasBorder: Boolean
    ): RenderInstances {
        // Use cached render instances
        if (riCache != null &&
            !forceUpdate &&
            xMapOffPrev == xMapOff && yMapOffPrev == yMapOff &&
            horTilesNumPrev == horTilesNum && verTilesNumPrev == verTilesNum &&
            drawAreasBorderPrev == drawAreasBorder
        ) return riCache!!

        deallocateCache()
        framedAreas.clear()

        val planeLayers = RenderInstances()

        // Collect all items to self sorted map
        for (x in -ADDITIONAL_VIEW_RANGE until horTilesNum + ADDITIONAL_VIEW_RANGE) {
            for (y in -ADDITIONAL_VIEW_RANGE until verTilesNum + ADDITIONAL_VIEW_RANGE) {
                val tileX = xMapOff + x
                val tileY = yMapOff + y

                if (tileX < 1 || tileX > dmm.maxX || tileY < 1 || tileY > dmm.maxY) {
                    continue
                }

                val tile = dmm.getTile(tileX, tileY)!!

                val renderX = (tileX - 1) * dmm.iconSize
                val renderY = (tileY - 1) * dmm.iconSize

                var currentAreaType = TYPE_AREA

                // Collect render instances
                for (tileItemId in tile.unsafeTileItemsIDs()) {
                    val tileItem = TileItemProvider.getByID(tileItemId)

                    if (drawAreasBorder && tileItem.isType(TYPE_AREA)) {
                        currentAreaType = tileItem.type
                    }

                    if (LayersManager.isHiddenType(tileItem.type)) {
                        continue
                    }

                    planeLayers.get(tileItem.plane, tileItem.layer).add(
                        RenderInstanceProvider.allocateRenderInstance(renderX.toFloat(), renderY.toFloat(), tileItem)
                    )
                }

                // Collect data to draw areas borders
                if (drawAreasBorder) {
                    var dir = 0

                    if (isFramedBorder(dmm, tileX - 1, tileY, currentAreaType)) dir = dir or WEST
                    if (isFramedBorder(dmm, tileX + 1, tileY, currentAreaType)) dir = dir or EAST
                    if (isFramedBorder(dmm, tileX, tileY - 1, currentAreaType)) dir = dir or SOUTH
                    if (isFramedBorder(dmm, tileX, tileY + 1, currentAreaType)) dir = dir or NORTH

                    if (dir != 0) {
                        framedAreas.add(FramedArea(renderX, renderY, dir))
                    }
                }
            }
        }

        // Sort items on the same layer
        planeLayers.values.forEach { layers ->
            layers.values.forEach { layer ->
                layer.sortWith(RenderComparator)
            }
        }

        riCache = planeLayers
        xMapOffPrev = xMapOff
        yMapOffPrev = yMapOff
        horTilesNumPrev = horTilesNum
        verTilesNumPrev = verTilesNum
        drawAreasBorderPrev = drawAreasBorder

        return planeLayers
    }

    private fun isFramedBorder(dmm: Dmm, x: Int, y: Int, currentAreaType: String): Boolean {
        dmm.getTile(x, y)?.let { tile ->
            tile.tileItems.find { it.isType(TYPE_AREA) }?.let { area ->
                return currentAreaType != area.type
            }
        }
        return false
    }

    private fun deallocateCache() {
        if (riCache != null) {
            riCache!!.values.forEach { planes ->
                planes.values.forEach { layers ->
                    RenderInstanceStruct.deallocate(layers)
                }
            }
        }
    }
}
