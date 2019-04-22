package io.github.spair.strongdmm.gui.instancelist

import io.github.spair.strongdmm.diDirect
import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.ViewController
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasController
import io.github.spair.strongdmm.gui.tabbedobjpanel.TabbedObjectPanelController
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.VAR_ICON
import io.github.spair.strongdmm.logic.dme.VAR_ICON_STATE
import io.github.spair.strongdmm.logic.dme.VAR_NAME

class InstanceListController : ViewController<InstanceListView>(diDirect()) {

    private val mapCanvasController by diInstance<MapCanvasController>()
    private val tabbedObjectPanelController by diInstance<TabbedObjectPanelController>()

    private var selectedType = ""

    fun findAndSelectInstancesByType(type: String) {
        selectedType = type

        val items = LinkedHashSet<ListItemInstance>()
        val dmeItem = Environment.dme.getItem(type)!!

        items.add(
            ListItemInstance(
                dmeItem.getVar(VAR_NAME) ?: "",
                dmeItem.getVarText(VAR_ICON) ?: "",
                dmeItem.getVarText(VAR_ICON_STATE) ?: ""
            )
        )

        mapCanvasController.selectedMap?.let { dmm ->
            dmm.getAllTileItemsByType(type).forEach { tileItem ->
                items.add(ListItemInstance(
                    tileItem.getVar(VAR_NAME) ?: "", tileItem.icon, tileItem.iconState, tileItem.dir, tileItem.customVars)
                )
            }
        }

        view.addItemInstances(items)
        tabbedObjectPanelController.setInstanceCount(items.size)
    }

    fun updateSelectedInstanceInfo() {
        if (selectedType.isNotEmpty()) {
            findAndSelectInstancesByType(selectedType)
        }
    }
}
