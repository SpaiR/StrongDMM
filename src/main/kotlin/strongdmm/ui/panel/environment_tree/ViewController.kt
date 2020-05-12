package strongdmm.ui.panel.environment_tree

import imgui.enums.ImGuiTreeNodeFlags
import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerTileItemService
import strongdmm.ui.panel.environment_tree.model.TreeNode
import strongdmm.util.NfdUtil
import strongdmm.util.extension.getOrPut
import java.io.File

class ViewController(
    private val state: State
) : EventHandler {
    companion object {
        private const val TREE_NODES_CREATION_LIMIT_PER_CYCLE: Int = 25
    }

    fun doOpenEnvironment() {
        NfdUtil.selectFile("dme")?.let { file ->
            sendEvent(TriggerEnvironmentService.OpenEnvironment(file))
        }
    }

    fun doOpenEnvironment(environmentPath: String) {
        sendEvent(TriggerEnvironmentService.OpenEnvironment(File(environmentPath)))
    }

    fun doCollapseAll() {
        state.isDoCollapseAll = true
    }

    fun getEnvironmentNameFromPath(environmentPath: String): String {
        return environmentPath.replace('\\', '/').substringAfterLast('/')
    }

    fun startCycle() {
        state.isSelectedInCycle = false
        state.createdTeeNodesInCycle = 0
    }

    fun stopCycle() {
        state.isDoCollapseAll = false
    }

    fun getTreeNodeSelectedFlag(dmeItem: DmeItem): Int {
        return if (dmeItem.type == state.selectedTileItemType) ImGuiTreeNodeFlags.Selected else 0
    }

    fun isFilteredNode(dmeItem: DmeItem): Boolean {
        return dmeItem.type.contains(state.typeFilter.get())
    }

    fun getOrCreateTreeNode(dmeItem: DmeItem): TreeNode? {
        return when {
            state.treeNodes.containsKey(dmeItem.id) -> {
                state.treeNodes.get(dmeItem.id)
            }
            state.createdTeeNodesInCycle < TREE_NODES_CREATION_LIMIT_PER_CYCLE -> {
                state.createdTeeNodesInCycle++
                state.treeNodes.getOrPut(dmeItem.id) { TreeNode(dmeItem) }
            }
            else -> null
        }
    }

    fun selectType(type: String) {
        if (!state.isSelectedInCycle) {
            state.isSelectedInCycle = true
            sendEvent(TriggerTileItemService.ChangeSelectedTileItem(GlobalTileItemHolder.getOrCreate(type)))
        }
    }

    fun isPartOfOpenedSelectedType(type: String): Boolean = state.isDoOpenSelectedType && state.selectedTileItemType.startsWith(type)

    fun isSelectedTypeOpened(currentType: String): Boolean {
        if (state.isDoOpenSelectedType && state.selectedTileItemType == currentType) {
            state.isDoOpenSelectedType = false
            return true
        }

        return false
    }
}
