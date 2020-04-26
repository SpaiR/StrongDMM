package strongdmm.ui.layers_filter_panel

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiMouseCursor
import imgui.enums.ImGuiTreeNodeFlags
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dme.DmeItem
import strongdmm.util.imgui.*

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 400f
        private const val HEIGHT: Float = 450f

        private const val TITLE: String = "Layers Filter"

        private const val LEAF_FLAGS: Int = ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen
    }

    lateinit var viewController: ViewController

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        WindowUtil.setNextPosAndSizeCentered(WIDTH, HEIGHT)

        window(TITLE, state.isOpened) {
            if (state.currentEnvironment == null) {
                text("No types to filter")
                return@window
            }

            setNextItemWidth(-1f)
            inputText("##types_filter", state.typesFilter, "Types Filter")

            separator()

            child("tree_nodes", imGuiWindowFlags = ImGuiWindowFlags.HorizontalScrollbar) {
                showTreeNodes(state.currentEnvironment!!.getItem(TYPE_AREA)!!)
                showTreeNodes(state.currentEnvironment!!.getItem(TYPE_TURF)!!)
                showTreeNodes(state.currentEnvironment!!.getItem(TYPE_OBJ)!!)
                showTreeNodes(state.currentEnvironment!!.getItem(TYPE_MOB)!!)
            }
        }
    }

    private fun showTreeNodes(dmeItem: DmeItem) {
        if (state.typesFilter.length > 0) {
            showFilteredNodes(dmeItem)
        } else {
            showAllNodes(dmeItem)
        }
    }

    private fun showFilteredNodes(dmeItem: DmeItem) {
        if (viewController.isFilteredNode(dmeItem)) {
            showToggleButton(dmeItem)
            sameLine()
            treeNodeEx(dmeItem.type, LEAF_FLAGS)
        }

        dmeItem.children.forEach { child ->
            showFilteredNodes(state.currentEnvironment!!.getItem(child)!!)
        }
    }

    private fun showAllNodes(dmeItem: DmeItem) {
        showToggleButton(dmeItem)
        sameLine()

        if (dmeItem.children.isEmpty()) {
            treeNodeEx(dmeItem.type, LEAF_FLAGS)
        } else if (treeNode(dmeItem.type)) {
            dmeItem.children.forEach { child ->
                showAllNodes(state.currentEnvironment!!.getItem(child)!!)
            }

            treePop()
        }
    }

    private fun showToggleButton(dmeItem: DmeItem) {
        val isFilteredType = viewController.isFilteredType(dmeItem)
        val buttonColor = if (isFilteredType) RED32 else GREEN32

        pushStyleColor(ImGuiCol.Button, buttonColor)
        pushStyleColor(ImGuiCol.ButtonActive, buttonColor)
        pushStyleColor(ImGuiCol.ButtonHovered, buttonColor)

        if (smallButton(" ##layer_filter_${dmeItem.id}")) {
            viewController.doToggleTypeFilter(dmeItem, isFilteredType)
        }

        if (isItemHovered()) {
            setMouseCursor(ImGuiMouseCursor.Hand)
        }

        popStyleColor(3)
    }
}
