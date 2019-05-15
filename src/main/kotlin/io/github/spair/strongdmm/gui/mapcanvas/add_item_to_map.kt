package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.gui.instancelist.InstanceListView

fun MapGLRenderer.addItemToMap() {
    selectedMap?.let { map ->
        InstanceListView.selectedInstance?.let { selectedInstance ->
            map.addInstanceToMap(selectedInstance, xMouseMap, yMouseMap)
            Frame.update(true)
        }
    }
}
