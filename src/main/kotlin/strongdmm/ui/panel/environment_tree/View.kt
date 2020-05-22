package strongdmm.ui.panel.environment_tree

import imgui.ImGui.*
import imgui.enums.ImGuiTreeNodeFlags
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.DmeItem
import strongdmm.ui.panel.environment_tree.model.TreeNode
import strongdmm.util.icons.ICON_FA_MINUS
import strongdmm.util.imgui.*

class View(
    private val state: State
) {
    companion object {
        private const val POS_X: Float = 10f
        private const val POS_Y: Float = 30f

        private const val WIDTH: Float = 330f
        private const val HEIGHT_PERCENT: Int = 56

        private const val TITLE: String = "Environment Tree"

        private const val ICON_SIZE: Float = 13f
    }

    lateinit var viewController: ViewController

    fun process() {
        WindowUtil.setNextPosAndSize(POS_X, POS_Y, WIDTH, WindowUtil.getHeightPercent(HEIGHT_PERCENT))

        window(TITLE) {
            if (state.currentEnvironment == null) {
                if (state.isEnvironmentLoading) {
                    textDisabled("Loading Environment...")
                } else {
                    showEnvironmentOpenControls()
                }

                return@window
            }

            viewController.startCycle()

            pushID(state.currentEnvironment!!.absRootDirPath)
            showControls()
            separator()
            showNodes(state.currentEnvironment!!)
            popID()

            viewController.stopCycle()
        }
    }

    private fun showEnvironmentOpenControls() {
        button("Open Environment...", block = viewController::doOpenEnvironment)

        separator()

        if (state.providedRecentEnvironments.isNotEmpty()) {
            text("Recent Environments:")
            state.providedRecentEnvironments.toTypedArray().forEach { environmentPath ->
                alignTextToFramePadding()
                bullet()
                sameLine()
                button(viewController.getEnvironmentNameFromPath(environmentPath) + "##$environmentPath") {
                    viewController.doOpenEnvironment(environmentPath)
                }
                setItemHoveredTooltip(environmentPath)
            }
        }
    }

    private fun showControls() {
        button("$ICON_FA_MINUS##collapse_all", block = viewController::doCollapseAll)
        setItemHoveredTooltip("Collapse All")
        sameLine()
        setNextItemWidth(-1f)
        inputText("##types_filter", state.typeFilter, "Types Filter")
    }

    private fun showNodes(environment: Dme) {
        child("tree_nodes", imGuiWindowFlags = ImGuiWindowFlags.HorizontalScrollbar) {
            showTreeNodes(environment, environment.getItem(TYPE_AREA)!!)
            showTreeNodes(environment, environment.getItem(TYPE_TURF)!!)
            showTreeNodes(environment, environment.getItem(TYPE_OBJ)!!)
            showTreeNodes(environment, environment.getItem(TYPE_MOB)!!)
        }
    }

    private fun showTreeNodes(environment: Dme, dmeItem: DmeItem) {
        if (state.typeFilter.length > 0) {
            showFilteredTreeNodes(environment, dmeItem)
        } else {
            showAllNodes(environment, dmeItem)
        }
    }

    private fun showFilteredTreeNodes(environment: Dme, dmeItem: DmeItem) {
        val selectedFlag = viewController.getTreeNodeSelectedFlag(dmeItem)

        if (viewController.isFilteredNode(dmeItem)) {
            val treeNode = viewController.getOrCreateTreeNode(dmeItem) ?: return

            showTreeNodeImage(treeNode)
            treeNodeEx(dmeItem.type, ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen or selectedFlag)

            if (isItemClicked()) {
                viewController.selectType(dmeItem.type)
            }
        }

        dmeItem.children.forEach { child ->
            showFilteredTreeNodes(environment, environment.getItem(child)!!)
        }
    }

    private fun showAllNodes(environment: Dme, dmeItem: DmeItem) {
        val selectedFlag = viewController.getTreeNodeSelectedFlag(dmeItem)
        val treeNode = viewController.getOrCreateTreeNode(dmeItem) ?: return

        showTreeNodeImage(treeNode)

        if (dmeItem.children.isEmpty()) {
            treeNodeEx(treeNode.name, ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen or selectedFlag)

            if (viewController.isSelectedTypeOpened(dmeItem.type)) {
                setScrollHereY()
            }
        } else {
            if (state.isDoCollapseAll) {
                setNextItemOpen(false)
            } else if (viewController.isPartOfOpenedSelectedType(dmeItem.type)) {
                setNextItemOpen(true)
            }

            if (treeNodeEx(treeNode.name, ImGuiTreeNodeFlags.OpenOnArrow or ImGuiTreeNodeFlags.OpenOnDoubleClick or selectedFlag) || state.isDoCollapseAll) {
                if (viewController.isSelectedTypeOpened(dmeItem.type)) {
                    setScrollHereY()
                }

                if (isItemClicked()) {
                    viewController.selectType(dmeItem.type)
                }

                if (state.isDoCollapseAll) {
                    treePush(treeNode.name)
                }

                dmeItem.children.forEach { child ->
                    showAllNodes(environment, environment.getItem(child)!!)
                }

                treePop()
            }
        }

        if (isItemClicked()) {
            viewController.selectType(dmeItem.type)
        }
    }

    private fun showTreeNodeImage(treeNode: TreeNode) {
        treeNode.sprite.run { image(textureId, ICON_SIZE, ICON_SIZE, u1, v1, u2, v2) }
        sameLine()
    }
}
