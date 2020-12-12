package strongdmm.ui.dialog.unknown_types

import imgui.ImGui
import strongdmm.event.EventBus
import strongdmm.event.Reaction
import strongdmm.service.map.UnknownType
import strongdmm.application.window.Window

class ViewController(
    private val state: State
) {
    fun doSetNewType(unknownType: UnknownType) {
        unknownType.type = state.inputStr.get()
    }

    fun doAddVariable(unknownType: UnknownType) {
        unknownType.variables.add(UnknownType.Variable("", ""))
    }

    fun doRemoveVariable(unknownType: UnknownType, variable: UnknownType.Variable) {
        state.variableToRemove = Pair(unknownType, variable)
    }

    fun doSetVariableName(variable: UnknownType.Variable) {
        variable.name = state.inputStr.get()
    }

    fun doSetVariableValue(variable: UnknownType.Variable) {
        variable.value = state.inputStr.get()
    }

    fun doContinue() {
        if (!state.hasUnknownTypes) {
            state.eventToReply?.reply(Unit)
            dispose()
        }
    }

    fun doCancel() {
        dispose()
    }

    fun getVariablesHeight(unknownType: UnknownType): Float {
        return unknownType.variables.size * ImGui.getFrameHeight() * Window.pointSize
    }

    fun blockApplication() {
        EventBus.post(Reaction.ApplicationBlockChanged(true))
    }

    fun checkVariableToRemove() {
        state.variableToRemove?.let { (unknownType, variable) ->
            state.unknownTypes.find { it == unknownType }?.variables?.remove(variable)
            state.variableToRemove = null
        }
    }

    fun checkHasUnknownTypes() {
        state.hasUnknownTypes = false
    }

    fun isUnknownType(type: String): Boolean {
        val flag = type.isNotEmpty() && state.currentEnvironment?.getItem(type) == null

        if (flag) {
            state.hasUnknownTypes = true
        }

        return flag
    }

    private fun dispose() {
        ImGui.closeCurrentPopup()
        EventBus.post(Reaction.ApplicationBlockChanged(false))
        state.unknownTypes = emptySet()
        state.eventToReply = null
    }
}
