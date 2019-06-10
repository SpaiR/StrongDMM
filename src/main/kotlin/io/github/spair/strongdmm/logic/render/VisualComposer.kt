package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.LayersManager
import io.github.spair.strongdmm.logic.map.TileItemProvider
import java.util.TreeMap

typealias RenderInstances = TreeMap<Float, TreeMap<Float, MutableList<RenderInstance>>>

private fun RenderInstances.get(plane: Float, layer: Float): MutableList<RenderInstance> {
    return computeIfAbsent(plane) { TreeMap() }.computeIfAbsent(layer) { mutableListOf() }
}

object VisualComposer {

    // We will look for things outside of viewport range to handle big objects (bigger then /world/icon_size)
    private const val ADDITIONAL_VIEW_RANGE = 2

    private var xMapOffPrev: Int = 0
    private var yMapOffPrev: Int = 0
    private var horTilesNumPrev: Int = 0
    private var verTilesNumPrev: Int = 0

    private var riCache: RenderInstances? = null
    var hasIncompleteJob = false

    fun clearCache() {
        riCache = null
        hasIncompleteJob = false
    }

    fun composeFrame(
        dmm: Dmm,
        xMapOff: Int,
        yMapOff: Int,
        horTilesNum: Int,
        verTilesNum: Int,
        forceUpdate: Boolean
    ): RenderInstances {
        // Use cached render instances
        if (riCache != null
            && !hasIncompleteJob && !forceUpdate
            && xMapOffPrev == xMapOff && yMapOffPrev == yMapOff
            && horTilesNumPrev == horTilesNum && verTilesNumPrev == verTilesNum
        ) return riCache!!

        hasIncompleteJob = false
        val planesLayers = RenderInstances()

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

                for (tileItemId in tile.unsafeGetTileItemsIDs()) {
                    val tileItem = TileItemProvider.getByID(tileItemId)

                    if (LayersManager.isHiddenType(tileItem.type)) {
                        continue
                    }

                    planesLayers.get(tileItem.plane, tileItem.layer).let {
                        it.add(RenderInstanceProvider.create(renderX.toFloat(), renderY.toFloat(), tileItem))

                        if (RenderInstanceProvider.hasInProcessImage) {
                            hasIncompleteJob = true
                        }
                    }
                }
            }
        }

        // Sort items on the same layer
        for (plane in planesLayers.values) {
            for (layer in plane.values) {
                layer.sortWith(RenderComparator)
            }
        }

        riCache = planesLayers
        xMapOffPrev = xMapOff
        yMapOffPrev = yMapOff
        horTilesNumPrev = horTilesNum
        verTilesNumPrev = verTilesNum

        return planesLayers
    }
}
