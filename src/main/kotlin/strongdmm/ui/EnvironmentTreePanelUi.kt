package strongdmm.ui

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui
import imgui.ImGui.checkbox
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
import strongdmm.byond.*
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmi.IconSprite
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.imgui.itemClicked
import strongdmm.util.imgui.itemHovered

class EnvironmentTreePanelUi : EventConsumer, EventSender {
    private var currentEnv: Dme? = null
    private val treeNodes: MutableMap<String, TreeNode> = mutableMapOf()

    private var selectedType: String = ""
    private var isSelectedInCycle: Boolean = false

    private var isShowIcons: MutableProperty0<Boolean> = MutableProperty0(true)

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

            checkbox("##show_icons", isShowIcons).itemHovered {
                tooltip { text("Show icons") }
            }

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
        val selectedFlag = if (dmeItem.type == selectedType) TreeNodeFlag.Selected.i else 0
        val treeNode = treeNodes.getOrPut(dmeItem.type) { TreeNode(dmeItem) }

        if (isShowIcons.get()) {
            treeNode.sprite.run { ImGui.image(textureId, Vec2(13, 13), Vec2(u1, v1), Vec2(u2, v2)) }
            sameLine()
        }

        if (dmeItem.children.isEmpty()) {
            treeNodeEx(treeNode.name, flags = TreeNodeFlag.Leaf or TreeNodeFlag.NoTreePushOnOpen or selectedFlag)
        } else {
            if (ImGui.treeNodeEx(treeNode.name, flags = TreeNodeFlag.OpenOnArrow or TreeNodeFlag.OpenOnDoubleClick or selectedFlag)) {
                itemClicked { selectType(dmeItem.type) }

                dmeItem.children.forEach { child ->
                    createTreeNodes(currentEnv!!.getItem(child)!!)
                }

                treePop()
            }
        }

        itemClicked { selectType(dmeItem.type) }
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
