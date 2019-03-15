package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.common.ViewController
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.render.MapDrawerGL
import org.kodein.di.direct
import org.kodein.di.erased.instance

class MapCanvasController : ViewController<MapCanvasView>(DI.direct.instance()) {

    private lateinit var selectedMap: Dmm
    private val mapDrawerGL: MapDrawerGL by lazy { MapDrawerGL(view.canvas) }

    override fun init() {
    }

    fun selectMap(dmm: Dmm) {
        selectedMap = dmm
        mapDrawerGL.switchMap(dmm)
    }
}
