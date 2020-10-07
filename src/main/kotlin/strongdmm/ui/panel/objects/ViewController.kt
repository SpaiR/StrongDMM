package strongdmm.ui.panel.objects

import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmi.IconSprite
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerInstanceService
import strongdmm.event.type.service.TriggerTileItemService
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.event.type.ui.TriggerInstanceLocatorPanelUi

class ViewController(
    private val state: State
) {
    fun doSelectItem(tileItem: TileItem) {
        EventBus.post(TriggerTileItemService.ChangeSelectedTileItem(tileItem))
        state.scrolledToItem = true // do not scroll panel in the next cycle
    }

    fun doFindInstanceOnMap(tileItem: TileItem) {
        EventBus.post(TriggerInstanceLocatorPanelUi.SearchById(tileItem.id))
    }

    fun doFindObjectOnMap(tileItem: TileItem) {
        EventBus.post(TriggerInstanceLocatorPanelUi.SearchByType(tileItem.type))
    }

    fun doNewInstance(tileItem: TileItem) {
        EventBus.post(TriggerEditVarsDialogUi.OpenWithTileItem(tileItem))
    }

    fun doEditInstance(tileItem: TileItem) {
        EventBus.post(TriggerInstanceService.EditInstance(tileItem))
    }

    fun doDeleteInstance(tileItem: TileItem) {
        EventBus.post(TriggerInstanceService.DeleteInstance(tileItem))
    }

    fun doGenerateInstancesFromIconStates(tileItem: TileItem) {
        EventBus.post(TriggerInstanceService.GenerateInstancesFromIconStates(tileItem))
    }

    fun doGenerateInstancesFromDirections(tileItem: TileItem) {
        EventBus.post(TriggerInstanceService.GenerateInstancesFromDirections(tileItem))
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
