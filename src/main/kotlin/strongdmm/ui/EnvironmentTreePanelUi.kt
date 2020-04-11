package strongdmm.ui

import gnu.trove.map.hash.TLongObjectHashMap
import imgui.ImBool
import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiTreeNodeFlags
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.*
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmi.IconSprite
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerEnvironmentController
import strongdmm.event.type.controller.TriggerTileItemController
import strongdmm.util.NfdUtil
import strongdmm.util.extension.getOrPut
import strongdmm.util.imgui.*
import strongdmm.window.AppWindow
import java.io.File

class EnvironmentTreePanelUi : EventConsumer, EventSender {
    companion object {
        private const val ICON_SIZE: Float = 13f
        private const val TREE_NODES_CREATION_LIMIT_PER_CYCLE: Int = 25
        private const val MIN_FILTER_CHARS: Int = 1
    }

    private var isEnvironmentLoading: Boolean = false

    private lateinit var providedRecentEnvironments: List<String>

    private var currentEnv: Dme? = null
    private val treeNodes: TLongObjectHashMap<TreeNode> = TLongObjectHashMap()

    private var activeTileItemType: String = ""
    private var isSelectedInCycle: Boolean = false

    private val isShowIcons: ImBool = ImBool(true)
    private val typeFilter: ImString = ImString(50)

    private var createdTeeNodesInCycle: Int = 0

    init {
        consumeEvent(Reaction.EnvironmentLoading::class.java, ::handleEnvironmentLoading)
        consumeEvent(Reaction.EnvironmentLoaded::class.java, ::handleEnvironmentLoaded)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(Provider.RecentFilesControllerRecentEnvironments::class.java, ::handleRecentFilesControllerRecentEnvironments)
    }

    fun process() {
        setNextWindowPos(10f, 30f, AppWindow.defaultWindowCond)
        setNextWindowSize(330f, AppWindow.windowHeight / 1.8f, AppWindow.defaultWindowCond)

        window("Environment Tree") {
            if (currentEnv == null) {
                if (isEnvironmentLoading) {
                    textDisabled("Loading Environment...")
                } else {
                    showEnvironmentOpenControls()
                }
                return@window
            }

            isSelectedInCycle = false
            createdTeeNodesInCycle = 0

            pushID(currentEnv!!.absRootDirPath)

            checkbox("##show_icons", isShowIcons)
            setItemHoveredTooltip("Show icons")
            sameLine()
            setNextItemWidth(-1f)
            inputText("##types_filter", typeFilter, "Types Filter", "Provide at least $MIN_FILTER_CHARS char to apply")

            separator()

            child("tree_nodes", imGuiWindowFlags = ImGuiWindowFlags.HorizontalScrollbar) {
                createTreeNodes(currentEnv!!.getItem(TYPE_AREA)!!)
                createTreeNodes(currentEnv!!.getItem(TYPE_TURF)!!)
                createTreeNodes(currentEnv!!.getItem(TYPE_OBJ)!!)
                createTreeNodes(currentEnv!!.getItem(TYPE_MOB)!!)
            }

            popID()
        }
    }

    private fun showEnvironmentOpenControls() {
        button("Open Environment...") {
            NfdUtil.selectFile("dme")?.let { file ->
                sendEvent(TriggerEnvironmentController.OpenEnvironment(file))
            }
        }

        separator()

        if (providedRecentEnvironments.isNotEmpty()) {
            text("Recent Environments:")
            providedRecentEnvironments.toTypedArray().forEach { envPath ->
                alignTextToFramePadding()
                bullet()
                sameLine()
                button(envPath.replace('\\', '/').substringAfterLast('/') + "##$envPath") {
                    sendEvent(TriggerEnvironmentController.OpenEnvironment(File(envPath)))
                }
                setItemHoveredTooltip(envPath)
            }
        }
    }

    private fun createTreeNodes(dmeItem: DmeItem) {
        val selectedFlag = if (dmeItem.type == activeTileItemType) ImGuiTreeNodeFlags.Selected else 0

        if (typeFilter.length >= MIN_FILTER_CHARS) {
            if (dmeItem.type.contains(typeFilter.get())) {
                val treeNode = getOrCreateTreeNode(dmeItem) ?: return
                showTreeNodeImage(treeNode)
                treeNodeEx(dmeItem.type, ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen or selectedFlag)
                if (isItemClicked()) {
                    selectType(dmeItem.type)
                }
            }

            dmeItem.children.forEach { child ->
                createTreeNodes(currentEnv!!.getItem(child)!!)
            }
        } else {
            val treeNode = getOrCreateTreeNode(dmeItem) ?: return
            showTreeNodeImage(treeNode)

            if (dmeItem.children.isEmpty()) {
                treeNodeEx(treeNode.name, ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.NoTreePushOnOpen or selectedFlag)
            } else {
                if (treeNodeEx(treeNode.name, ImGuiTreeNodeFlags.OpenOnArrow or ImGuiTreeNodeFlags.OpenOnDoubleClick or selectedFlag)) {
                    if (isItemClicked()) {
                        selectType(dmeItem.type)
                    }

                    dmeItem.children.forEach { child ->
                        createTreeNodes(currentEnv!!.getItem(child)!!)
                    }

                    treePop()
                }
            }

            if (isItemClicked()) {
                selectType(dmeItem.type)
            }
        }
    }

    private fun getOrCreateTreeNode(dmeItem: DmeItem): TreeNode? {
        return when {
            treeNodes.containsKey(dmeItem.id) -> {
                treeNodes.get(dmeItem.id)
            }
            createdTeeNodesInCycle < TREE_NODES_CREATION_LIMIT_PER_CYCLE -> {
                createdTeeNodesInCycle++
                treeNodes.getOrPut(dmeItem.id) { TreeNode(dmeItem) }
            }
            else -> null
        }
    }

    private fun showTreeNodeImage(treeNode: TreeNode) {
        if (isShowIcons.get()) {
            treeNode.sprite.run { image(textureId, ICON_SIZE, ICON_SIZE, u1, v1, u2, v2) }
            sameLine()
        }
    }

    private fun selectType(type: String) {
        if (!isSelectedInCycle) {
            sendEvent(TriggerTileItemController.ChangeActiveTileItem(GlobalTileItemHolder.getOrCreate(type)))
            isSelectedInCycle = true
        }
    }

    private fun handleEnvironmentLoading() {
        isEnvironmentLoading = true
    }

    private fun handleEnvironmentLoaded() {
        isEnvironmentLoading = false
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        currentEnv = event.body
    }

    private fun handleEnvironmentReset() {
        typeFilter.set("")
        currentEnv = null
        treeNodes.clear()
    }

    private fun handleActiveTileItemChanged(event: Event<TileItem?, Unit>) {
        activeTileItemType = event.body?.type ?: ""
    }

    private fun handleRecentFilesControllerRecentEnvironments(event: Event<List<String>, Unit>) {
        providedRecentEnvironments = event.body
    }

    private class TreeNode(dmeItem: DmeItem) {
        val name: String = dmeItem.type.substringAfterLast('/')
        val sprite: IconSprite

        init {
            val icon = dmeItem.getVarText(VAR_ICON) ?: ""
            val iconState = dmeItem.getVarText(VAR_ICON_STATE) ?: ""
            sprite = GlobalDmiHolder.getIconSpriteOrPlaceholder(icon, iconState)
        }
    }
}
