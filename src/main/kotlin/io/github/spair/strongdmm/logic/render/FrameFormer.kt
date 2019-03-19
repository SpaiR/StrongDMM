package io.github.spair.strongdmm.logic.render

import io.github.spair.byond.ByondVars
import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.logic.map.Dmm
import org.kodein.di.erased.instance
import java.util.*

private const val ADDITIONAL_VIEW_RANGE = 2

class FrameFormer {

    private val renderInstanceProvider by DI.instance<RenderInstanceProvider>()

    fun buildFrame(
        dmm: Dmm,
        xMapOff: Int,
        yMapOff: Int,
        horTilesNum: Int,
        verTilesNum: Int
    ): TreeMap<Double, TreeMap<Double, List<RenderInstance>>> {
        val planesLayers = TreeMap<Double, TreeMap<Double, List<RenderInstance>>>()

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
                    val plane = tileItem.getVarDouble(ByondVars.PLANE)
                    val layer = tileItem.getVarDouble(ByondVars.LAYER)

                    planesLayers.computeIfAbsent(plane) { TreeMap() }.computeIfAbsent(layer) { arrayListOf() }.let {
                        (it as ArrayList).add(renderInstanceProvider.create(renderX, renderY, tileItem))
                    }
                }
            }
        }

        return planesLayers
    }
}
