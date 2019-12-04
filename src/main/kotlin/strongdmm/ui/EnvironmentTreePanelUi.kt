package strongdmm.ui

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui.checkbox
import imgui.ImGui.image
import imgui.ImGui.inputText
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.ImGui.treeNodeEx
import imgui.ImGui.treePop
import imgui.MutableProperty0
import imgui.TreeNodeFlag
import imgui.dsl_.*
import imgui.internal.strlen
import strongdmm.byond.*
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmi.IconSprite
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.imgui.itemAction
import strongdmm.util.imgui.itemClicked
import strongdmm.util.imgui.itemHovered

class EnvironmentTreePanelUi : EventConsumer, EventSender {
    companion object {
        private val ICON_SIZE: Vec2 = Vec2(13, 13)
    }

    private var currentEnv: Dme? = null
    private val treeNodes: MutableMap<String, TreeNode> = mutableMapOf()

    private var selectedType: String = ""
    private var isSelectedInCycle: Boolean = false

    private val isShowIcons: MutableProperty0<Boolean> = MutableProperty0(true)
    private val isShowTypes: MutableProperty0<Boolean> = MutableProperty0(false)

    private val typeFilterRaw: CharArray = CharArray(100)
    private var typeFilter: String = ""

    init {
        consumeEvent(Event.Global.SwitchEnvironment::class.java, ::handleSwitchEnvironment)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
    }

    fun process() {
        isSelectedInCycle = false

        setNextWindowPos(Vec2(10, 30), Cond.Once)
        setNextWindowSize(Vec2(300, 500), Cond.Once)

        window("Environment Tree") {
            if (currentEnv == null) {
                text("No environment opened")
                return@window
            }

            inputText("Filter", typeFilterRaw).itemAction {
                typeFilter = String(typeFilterRaw, 0, typeFilterRaw.strlen)
            }

            checkbox("Show icons", isShowIcons)
            sameLine()
            checkbox("Show types", isShowTypes)

            separator()

            child("tree_nodes") {
                createTreeNodes(currentEnv!!.getItem(TYPE_AREA)!!)
                createTreeNodes(currentEnv!!.getItem(TYPE_TURF)!!)
                createTreeNodes(currentEnv!!.getItem(TYPE_OBJ)!!)
                createTreeNodes(currentEnv!!.getItem(TYPE_MOB)!!)
            }
        }
    }

    private fun createTreeNodes(dmeItem: DmeItem) {
        val treeNode = treeNodes.getOrPut(dmeItem.type) { TreeNode(dmeItem) }
        val selectedFlag = if (dmeItem.type == selectedType) TreeNodeFlag.Selected.i else 0

        if (typeFilter.isNotEmpty()) {
            if (dmeItem.type.contains(typeFilter)) {
                showTreeNodeImage(treeNode)
                treeNodeEx(dmeItem.type, flags = TreeNodeFlag.Leaf or TreeNodeFlag.NoTreePushOnOpen or selectedFlag)
                itemClicked { selectType(dmeItem.type) }
            }

            dmeItem.children.forEach { child ->
                createTreeNodes(currentEnv!!.getItem(child)!!)
            }
        } else {
            var isTooltipShown = false
            showTreeNodeImage(treeNode)

            if (dmeItem.children.isEmpty()) {
                treeNodeEx(treeNode.name, flags = TreeNodeFlag.Leaf or TreeNodeFlag.NoTreePushOnOpen or selectedFlag)
                showTypeTooltip(dmeItem.type)
                isTooltipShown = true
            } else {
                if (treeNodeEx(treeNode.name, flags = TreeNodeFlag.OpenOnArrow or TreeNodeFlag.OpenOnDoubleClick or selectedFlag)) {
                    itemClicked { selectType(dmeItem.type) }

                    showTypeTooltip(dmeItem.type)
                    isTooltipShown = true

                    dmeItem.children.forEach { child ->
                        createTreeNodes(currentEnv!!.getItem(child)!!)
                    }

                    treePop()
                }
            }

            itemClicked { selectType(dmeItem.type) }

            if (!isTooltipShown) {
                showTypeTooltip(dmeItem.type)
            }
        }
    }

    private fun showTreeNodeImage(treeNode: TreeNode) {
        if (isShowIcons.get()) {
            treeNode.sprite.run { image(textureId, ICON_SIZE, Vec2(u1, v1), Vec2(u2, v2)) }
            sameLine()
        }
    }

    private fun showTypeTooltip(type: String) {
        if (isShowTypes.get()) {
            itemHovered { tooltip { text(type) } }
        }
    }

    private fun selectType(type: String) {
        if (!isSelectedInCycle) {
            selectedType = type
            isSelectedInCycle = true
        }
    }

    private fun handleSwitchEnvironment(event: Event<Dme, Unit>) {
        currentEnv = event.body
    }

    private fun handleResetEnvironment() {
        currentEnv = null
        treeNodes.clear()
    }

    private class TreeNode(dmeItem: DmeItem) {
        val name: String = dmeItem.type.substringAfterLast('/')
        val sprite: IconSprite

        init {
            val icon = dmeItem.getVarText(VAR_ICON) ?: ""
            val iconState = dmeItem.getVarText(VAR_ICON_STATE) ?: ""
            sprite = GlobalDmiHolder.getSprite(icon, iconState)
        }
    }
}
