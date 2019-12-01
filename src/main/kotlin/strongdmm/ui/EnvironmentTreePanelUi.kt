package strongdmm.ui

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui
import imgui.ImGui.sameLine
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.setNextWindowSize
import imgui.MutableProperty0
import imgui.TreeNodeFlag
import imgui.dsl_.menuBar
import imgui.dsl_.window
import strongdmm.byond.*
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmi.IconSprite
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.util.LMB

class EnvironmentTreePanelUi : EventConsumer {
    private var currentEnv: Dme? = null
    private val treeNodes: MutableMap<String, TreeNode> = mutableMapOf()

    private var selectedType: String = ""
    private var isSelectedInCycle: Boolean = false

    private var isShowIcons: MutableProperty0<Boolean> = MutableProperty0(true)

    init {
        consumeEvent(Event.Global.SwitchEnvironment::class.java, ::handleSwitchEnvironment)
    }

    fun process() {
        isSelectedInCycle = false

        setNextWindowPos(Vec2(10, 30), Cond.Once)
        setNextWindowSize(Vec2(300, 500), Cond.Once)

        window("Environment Tree") {
            if (currentEnv == null) {
                return@window
            }

            menuBar {
                ImGui.checkbox("Show icons", isShowIcons)
            }

            createTreeNodes(currentEnv!!.getItem(TYPE_AREA)!!)
            createTreeNodes(currentEnv!!.getItem(TYPE_TURF)!!)
            createTreeNodes(currentEnv!!.getItem(TYPE_OBJ)!!)
            createTreeNodes(currentEnv!!.getItem(TYPE_MOB)!!)
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
            ImGui.treeNodeEx(treeNode.name, flags = TreeNodeFlag.Leaf or TreeNodeFlag.NoTreePushOnOpen or selectedFlag)
        } else {
            if (ImGui.treeNodeEx(treeNode.name, flags = TreeNodeFlag.OpenOnArrow or TreeNodeFlag.OpenOnDoubleClick or selectedFlag)) {
                if (ImGui.isItemClicked(LMB) && !isSelectedInCycle) {
                    selectedType = dmeItem.type
                    isSelectedInCycle = true
                }

                dmeItem.children.forEach { child ->
                    createTreeNodes(currentEnv!!.getItem(child)!!)
                }

                ImGui.treePop()
            }
        }

        if (ImGui.isItemClicked(LMB) && !isSelectedInCycle) {
            selectedType = dmeItem.type
            isSelectedInCycle = true
        }
    }

    private fun handleSwitchEnvironment(event: Event<Dme, Unit>) {
        currentEnv = event.body
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
