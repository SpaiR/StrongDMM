package strongdmm.ui.dialog.edit_vars

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiMouseCursor
import imgui.enums.ImGuiStyleVar
import org.lwjgl.glfw.GLFW
import strongdmm.ui.dialog.edit_vars.model.Variable
import strongdmm.util.imgui.*

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 500f
        private const val HEIGHT: Float = 550f
    }

    lateinit var viewController: ViewController

    fun process() {
        viewController.getTileItem()?.let { tileItem ->
            WindowUtil.setNextPosAndSizeCentered(WIDTH, HEIGHT)

            window("Edit Variables: ${tileItem.type}##edit_variables_${state.windowId}") {
                showControls()

                separator()

                child("vars_table") {
                    if (state.isShowVarsByType.get()) {
                        showVariablesByType()
                    } else {
                        showAllVariables()
                    }
                }
            }

            viewController.checkPinnedVariables()
        }
    }

    private fun showControls() {
        if (state.isFistOpen) {
            setKeyboardFocusHere()
            state.isFistOpen = false
        }

        setNextItemWidth(getWindowWidth() - 105f)
        inputText("##vars_filter", state.varsFilter, "Variables Filter")
        sameLine()
        button("OK", block = viewController::doOk)
        sameLine()
        button("Cancel", block = viewController::doCancel)

        checkbox("Modified##is_show_modified_vars", state.isShowModifiedVars)
        sameLine()
        checkbox("By Type##is_show_vars_by_type", state.isShowVarsByType)

        if (state.isShowVarsByType.get()) {
            setNextItemWidth(-1f)
            inputText("##types_filter", state.typesFilter, "Types Filter")
        }
    }

    private fun showVariablesByType() {
        for ((type, variables) in state.variablesByType) {
            if (viewController.isFilteredOutType(type)) {
                continue
            }

            text(type)
            sameLine()
            textDisabled("(${variables.size})")
            columns(2, "variables_by_$type", true)
            variables.forEach(::showVariable)
            columns(1)
        }
    }

    private fun showAllVariables() {
        if (state.pinnedVariables.isNotEmpty()) {
            textColored(1f, .84f, 0f, 1f, "Pinned")
            columns(2, "pinned_edit_vars_columns", true)
            showPinnedVariables()
            columns(1)
            newLine()
            textDisabled("Other")
        }

        columns(2, "edit_vars_columns", true)
        showOtherVariables()
    }

    private fun showPinnedVariables() {
        state.pinnedVariables.forEach(::showVariable)
    }

    private fun showOtherVariables() {
        state.variables.forEach {
            if (!it.isPinned) {
                showVariable(it)
            }
        }
    }

    private fun showVariable(variable: Variable) {
        if (viewController.isFilteredOutVariable(variable)) {
            return
        }

        showVariablePinOption(variable)

        sameLine(0f, 15f)
        alignTextToFramePadding()

        if (variable.isModified || variable.isChanged) {
            textColored(0f, 1f, 0f, 1f, variable.name)
        } else {
            text(variable.name)
        }

        nextColumn()

        if (variable === state.currentEditVar) {
            showVariableEditField(variable)
        } else {
            showVariableValue(variable)
        }

        nextColumn()
        separator()
    }

    private fun showVariablePinOption(variable: Variable) {
        withStyleVar(ImGuiStyleVar.FramePadding, .25f, .25f) {
            if (radioButton("##variable_pin__${variable.hash}", variable.isPinned)) {
                viewController.doPinVariable(variable)
            }
        }
    }

    private fun showVariableEditField(variable: Variable) {
        setNextItemWidth(getColumnWidth(-1))

        if (!state.variableInputFocused) {
            setKeyboardFocusHere()
            state.variableInputFocused = true
        }

        inputText("##variable_edit_${variable.hash}", variable.value)

        if (isKeyPressed(GLFW.GLFW_KEY_ENTER) || isKeyPressed(GLFW.GLFW_KEY_KP_ENTER) || isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            viewController.doStopEdit()
        }
    }

    private fun showVariableValue(variable: Variable) {
        pushStyleColor(ImGuiCol.Button, 0)
        pushStyleColor(ImGuiCol.ButtonHovered, .25f, .58f, .98f, .5f)
        pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0f, 0f)

        button("${variable.value}##variable_value_${variable.hash}", getColumnWidth(-1)) {
            viewController.doStartEdit(variable)
        }

        if (isItemHovered()) {
            setMouseCursor(ImGuiMouseCursor.Hand)
        }

        popStyleVar()
        popStyleColor(2)
    }
}
