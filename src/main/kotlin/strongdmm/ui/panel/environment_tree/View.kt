package strongdmm.ui.panel.environment_tree

import imgui.ImGui.*
import imgui.ImGuiListClipper
import imgui.callback.ImListClipperCallback
import imgui.flag.ImGuiTreeNodeFlags
import imgui.flag.ImGuiWindowFlags
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.DmeItem
import strongdmm.ui.panel.environment_tree.model.TreeNode
import strongdmm.util.icons.ICON_FA_MINUS
import strongdmm.util.imgui.*
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val treeIndent: Float
            get() = 20f * Window.pointSize

        private val iconSize: Float
            get() = 16f * Window.pointSize
    }

    lateinit var viewController: ViewController

    private val filteredNodeClipCallback: ImListClipperCallback = object : ImListClipperCallback() {
        override fun accept(index: Int) {
            val treeNode = state.filteredTreeNodes[index]
            val dmeItem = treeNode.dmeItem
            val selectedFlag = viewController.getTreeNodeSelectedFlag(dmeItem)

            showTreeNodeImage(treeNode)
            treeNodeEx(dmeItem.type, ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen or selectedFlag)

            if (isItemClicked()) {
                viewController.selectType(dmeItem.type)
            }
        }
    }

    fun process() {
        setNextWindowPos(EnvironmentTreePanelUi.posX, EnvironmentTreePanelUi.posY, Window.windowCond)
        setNextWindowSize(EnvironmentTreePanelUi.width, EnvironmentTreePanelUi.height, Window.windowCond)

        window(viewController.getTitle()) {
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

        if (state.providedRecentEnvironmentsWithMaps.isNotEmpty()) {
            text("Recent Environments:")
            state.providedRecentEnvironmentsWithMaps.forEach { (environmentPath, maps) ->
                alignTextToFramePadding()
                bullet()
                sameLine()
                button(viewController.getEnvironmentNameFromPath(environmentPath) + "##$environmentPath") {
                    viewController.doOpenEnvironment(environmentPath)
                }
                ImGuiExt.setItemHoveredTooltip(environmentPath)

                maps.forEach { mapPath ->
                    withIndent(treeIndent) {
                        alignTextToFramePadding()
                        text("-")
                        sameLine()
                        smallButton(mapPath.fileName + "##${mapPath.absolute}") {
                            viewController.doOpenEnvironmentWithMap(environmentPath, mapPath)
                        }
                        ImGuiExt.setItemHoveredTooltip(mapPath.readable)
                    }
                }
            }
        }
    }

    private fun showControls() {
        button("$ICON_FA_MINUS##collapse_all", block = viewController::doCollapseAll)
        ImGuiExt.setItemHoveredTooltip("Collapse All")
        sameLine()
        setNextItemWidth(-1f)
        if (ImGuiExt.inputTextPlaceholder("##types_filter", state.typeFilter, "Types Filter")) {
            viewController.doCollectFilteredTreeNodes()
        }
    }

    private fun showNodes(environment: Dme) {
        child("tree_nodes", imGuiWindowFlags = ImGuiWindowFlags.HorizontalScrollbar) {
            if (state.filteredTreeNodes.isEmpty()) {
                showAllNodes(environment, environment.getItem(TYPE_AREA)!!)
                showAllNodes(environment, environment.getItem(TYPE_TURF)!!)
                showAllNodes(environment, environment.getItem(TYPE_OBJ)!!)
                showAllNodes(environment, environment.getItem(TYPE_MOB)!!)
            } else {
                showFilteredNodes()
            }
        }
    }

    private fun showFilteredNodes() {
        ImGuiListClipper.forEach(state.filteredTreeNodes.size, filteredNodeClipCallback)
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
        treeNode.sprite.run { image(textureId, iconSize, iconSize, u1, v1, u2, v2) }
        sameLine()
    }
}
