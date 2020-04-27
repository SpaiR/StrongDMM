package strongdmm.ui.panel.objects

import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmi.IconSprite
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerInstanceController
import strongdmm.event.type.controller.TriggerTileItemController
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.event.type.ui.TriggerInstanceLocatorPanelUi

class ViewController(
    private val state: State
) : EventHandler {
    fun doSelectItem(tileItem: TileItem) {
        sendEvent(TriggerTileItemController.ChangeSelectedTileItem(tileItem))
        state.scrolledToItem = true // do not scroll panel in the next cycle
    }

    fun doFindInstanceOnMap(tileItem: TileItem) {
        sendEvent(TriggerInstanceLocatorPanelUi.SearchById(tileItem.id))
    }

    fun doFindAllObjectsOnMap(tileItem: TileItem) {
        sendEvent(TriggerInstanceLocatorPanelUi.SearchByType(tileItem.type))
    }

    fun doNewInstance(tileItem: TileItem) {
        sendEvent(TriggerEditVarsDialogUi.OpenWithTileItem(tileItem))
    }

    fun doGenerateInstancesFromIconStates(tileItem: TileItem) {
        sendEvent(TriggerInstanceController.GenerateInstancesFromIconStates(tileItem) {
            updateTileItems()
        })
    }

    fun doGenerateInstancesFromDirections(tileItem: TileItem) {
        sendEvent(TriggerInstanceController.GenerateInstancesFromDirections(tileItem) {
            updateTileItems()
        })
    }

    fun updateTileItems() {
        if (state.selectedTileItemType.isNotEmpty()) {
            state.tileItems = getTileItemsByTypeSorted(state.selectedTileItemType)
        }
    }

    fun getTitle(): String {
        return if (state.tileItems?.size ?: 0 > 0) "(${state.tileItems!!.size}) ${state.selectedTileItemType}###object_panel" else "Object Panel###object_panel"
    }

    fun getIconSprite(tileItem: TileItem): IconSprite {
        return GlobalDmiHolder.getIconSpriteOrPlaceholder(tileItem.icon, tileItem.iconState, tileItem.dir)
    }

    fun getTileItemsByTypeSorted(type: String): List<TileItem> {
        val tileItems = GlobalTileItemHolder.getTileItemsByType(type).sortedBy { it.name }.sortedBy { it.iconState }.toMutableList()
        val initialItem = GlobalTileItemHolder.getOrCreate(type)
        tileItems.remove(initialItem)
        tileItems.add(0, initialItem)
        return tileItems
    }
}
