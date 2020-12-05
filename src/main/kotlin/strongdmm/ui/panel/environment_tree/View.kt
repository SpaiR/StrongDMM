package strongdmm.ui.panel.environment_tree

import imgui.ImGui
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
import strongdmm.ui.LayoutManager
import strongdmm.ui.panel.environment_tree.model.TreeNode
import strongdmm.util.icons.ICON_FA_MINUS
import strongdmm.util.imgui.*
import strongdmm.application.window.Window

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
            ImGui.treeNodeEx(dmeItem.type, ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen or selectedFlag)

            if (ImGui.isItemClicked()) {
                viewController.selectType(dmeItem.type)
            }
        }
    }

    fun process() {
        ImGui.setNextWindowPos(LayoutManager.Top.Left.posX, LayoutManager.Top.Left.posY, Window.windowCond)
        ImGui.setNextWindowSize(LayoutManager.Top.Left.width, LayoutManager.Top.Left.height, Window.windowCond)

        imGuiBegin(viewController.getTitle()) {
            if (state.currentEnvironment == null) {
                if (state.isEnvironmentLoading) {
                    ImGui.textDisabled("Loading Environment...")
                } else {
                    showEnvironmentOpenControls()
                }

                return@imGuiBegin
            }

            viewController.startCycle()

            ImGui.pushID(state.currentEnvironment!!.absRootDirPath)
            showControls()
            ImGui.separator()
            showNodes(state.currentEnvironment!!)
            ImGui.popID()

            viewController.stopCycle()
        }
    }

    private fun showEnvironmentOpenControls() {
        imGuiButton("Open Environment...", block = viewController::doOpenEnvironment)

        ImGui.separator()

        if (state.providedRecentEnvironmentsWithMaps.isNotEmpty()) {
            ImGui.text("Recent Environments:")
            state.providedRecentEnvironmentsWithMaps.forEach { (environmentPath, maps) ->
                ImGui.alignTextToFramePadding()
                ImGui.bullet()
                ImGui.sameLine()
                imGuiButton(viewController.getEnvironmentNameFromPath(environmentPath) + "##$environmentPath") {
                    viewController.doOpenEnvironment(environmentPath)
                }
                ImGuiExt.setItemHoveredTooltip(environmentPath)

                maps.forEach { mapPath ->
                    imGuiWithIndent(treeIndent) {
                        ImGui.alignTextToFramePadding()
                        ImGui.text("-")
                        ImGui.sameLine()
                        imGuiSmallButton(mapPath.fileName + "##${mapPath.absolute}") {
                            viewController.doOpenEnvironmentWithMap(environmentPath, mapPath)
                        }
                        ImGuiExt.setItemHoveredTooltip(mapPath.readable)
                    }
                }
            }
        }
    }

    private fun showControls() {
        imGuiButton("$ICON_FA_MINUS##collapse_all", block = viewController::doCollapseAll)
        ImGuiExt.setItemHoveredTooltip("Collapse All")
        ImGui.sameLine()
        ImGui.setNextItemWidth(-1f)
        if (ImGuiExt.inputTextPlaceholder("##types_filter", state.typeFilter, "Types Filter")) {
            viewController.doCollectFilteredTreeNodes()
        }
    }

    private fun showNodes(environment: Dme) {
        imGuiChild("tree_nodes", imGuiWindowFlags = ImGuiWindowFlags.HorizontalScrollbar) {
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
            ImGui.treeNodeEx(treeNode.name, ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen or selectedFlag)

            if (viewController.isSelectedTypeOpened(dmeItem.type)) {
                ImGui.setScrollHereY()
            }
        } else {
            if (state.isDoCollapseAll) {
                ImGui.setNextItemOpen(false)
            } else if (viewController.isPartOfOpenedSelectedType(dmeItem.type)) {
                ImGui.setNextItemOpen(true)
            }

            if (ImGui.treeNodeEx(treeNode.name, ImGuiTreeNodeFlags.OpenOnArrow or ImGuiTreeNodeFlags.OpenOnDoubleClick or selectedFlag) || state.isDoCollapseAll) {
                if (viewController.isSelectedTypeOpened(dmeItem.type)) {
                    ImGui.setScrollHereY()
                }

                if (ImGui.isItemClicked()) {
                    viewController.selectType(dmeItem.type)
                }

                if (state.isDoCollapseAll) {
                    ImGui.treePush(treeNode.name)
                }

                dmeItem.children.forEach { child ->
                    showAllNodes(environment, environment.getItem(child)!!)
                }

                ImGui.treePop()
            }
        }

        if (ImGui.isItemClicked()) {
            viewController.selectType(dmeItem.type)
        }
    }

    private fun showTreeNodeImage(treeNode: TreeNode) {
        treeNode.sprite.run { ImGui.image(textureId, iconSize, iconSize, u1, v1, u2, v2) }
        ImGui.sameLine()
    }
}
