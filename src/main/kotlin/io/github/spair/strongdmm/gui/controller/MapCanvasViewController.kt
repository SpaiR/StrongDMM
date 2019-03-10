package io.github.spair.strongdmm.gui.controller

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.view.MapCanvasView
import org.kodein.di.direct
import org.kodein.di.erased.instance

class MapCanvasViewController : ViewController<MapCanvasView>(DI.direct.instance()) {

    override fun init() {
    }
}
