package strongdmm.ui.panel.layers_filter

import imgui.ImGui.*
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiTreeNodeFlags
import imgui.flag.ImGuiWindowFlags
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dme.DmeItem
import strongdmm.util.imgui.*
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val WIDTH: Float
            get() = 400f * Window.pointSize
        private val HEIGHT: Float
            get() = 450f * Window.pointSize

        private const val TITLE: String = "Layers Filter"
        private const val LEAF_FLAGS: Int = ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen

        private val toggleButtonPadding: Float
            get() = Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        ImGuiUtil.setNextWindowCentered(WIDTH, HEIGHT)

        window(TITLE, state.isOpened) {
            if (state.currentEnvironment == null) {
                text("No types to filter")
                return@window
            }

            setNextItemWidth(-1f)
            ImGuiExt.inputTextPlaceholder("##types_filter", state.typesFilter, "Types Filter")

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

        withStyleVar(ImGuiStyleVar.FramePadding, toggleButtonPadding, toggleButtonPadding) {
            if (checkbox("##layer_filter_${dmeItem.id}", !isFilteredType)) {
                viewController.doToggleTypeFilter(dmeItem, isFilteredType)
            }
        }
    }
}
