package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.logic.map.Dmm
import java.util.*

typealias RenderInstances = TreeMap<Float, TreeMap<Float, MutableList<RenderInstance>>>

fun RenderInstances.get(plane: Float, layer: Float): MutableList<RenderInstance> {
    return computeIfAbsent(plane) { TreeMap() }.computeIfAbsent(layer) { mutableListOf() }
}

// We will look for things outside of viewport range to handle big objects (bigger then 32 px)
private const val ADDITIONAL_VIEW_RANGE = 2

class VisualComposer {

    private val riProvider by diInstance<RenderInstanceProvider>()

    private var xMapOffPrev: Int = 0
    private var yMapOffPrev: Int = 0
    private var horTilesNumPrev: Int = 0
    private var verTilesNumPrev: Int = 0

    private lateinit var riCache: RenderInstances
    var hasIncompleteJob = false

    fun composeFrame(
        dmm: Dmm,
        xMapOff: Int,
        yMapOff: Int,
        horTilesNum: Int,
        verTilesNum: Int,
        forceUpdate: Boolean
    ): RenderInstances {
        if (!hasIncompleteJob && !forceUpdate
            && xMapOffPrev == xMapOff && yMapOffPrev == yMapOff
            && horTilesNumPrev == horTilesNum && verTilesNumPrev == verTilesNum
        ) return riCache

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

                for (tileItem in tile) {
                    planesLayers.get(tileItem.plane, tileItem.layer).let {
                        it.add(riProvider.create(renderX, renderY, tileItem))

                        if (riProvider.hasInProcessImage) {
                            hasIncompleteJob = true
                        }
                    }
                }
            }
        }

        // Sort items on the same layer
        for (plane in planesLayers.values) {
            for (layer in plane.values) {
                Collections.sort(layer, RenderComparator)
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
