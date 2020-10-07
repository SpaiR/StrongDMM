package strongdmm.ui.panel.layers_filter

import strongdmm.byond.dme.DmeItem
import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerLayersFilterService

class ViewController(
    private val state: State
) {
    fun doToggleTypeFilter(dmeItem: DmeItem, isFilteredType: Boolean) {
        toggleTypeFilter(dmeItem, isFilteredType)
        EventBus.post(TriggerLayersFilterService.FilterLayersById(state.filteredTypesId.toArray()))
    }

    fun isFilteredNode(dmeItem: DmeItem): Boolean = dmeItem.type.contains(state.typesFilter.get())

    fun isFilteredType(dmeItem: DmeItem): Boolean = state.filteredTypesId.contains(dmeItem.id)

    private fun toggleTypeFilter(dmeItem: DmeItem, isFilteredType: Boolean) {
        if (isFilteredType) {
            state.filteredTypesId.remove(dmeItem.id)
            dmeItem.children.forEach {
                val item = state.currentEnvironment!!.getItem(it)!!
                state.filteredTypesId.remove(item.id)
                toggleTypeFilter(item, isFilteredType)
            }
        } else {
            state.filteredTypesId.add(dmeItem.id)
            dmeItem.children.forEach {
                val item = state.currentEnvironment!!.getItem(it)!!
                state.filteredTypesId.add(item.id)
                toggleTypeFilter(item, isFilteredType)
            }
        }
    }
}
