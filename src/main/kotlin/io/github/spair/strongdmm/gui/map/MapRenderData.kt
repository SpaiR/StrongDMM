package io.github.spair.strongdmm.gui.map

import io.github.spair.strongdmm.logic.map.Dmm

class MapRenderData(val dmm: Dmm) {
    // Visual offset to translate viewport
    var xViewOff = 0f
    var yViewOff = 0f

    // Map offset with coords for bottom-left point of the screen
    var xMapOff = 0
    var yMapOff = 0

    // Zooming stuff
    var viewZoom = 1f
    var currZoom = 5
}
