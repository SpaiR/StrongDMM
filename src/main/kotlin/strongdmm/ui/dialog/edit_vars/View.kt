package strongdmm.ui.dialog.edit_vars

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiMouseCursor
import imgui.flag.ImGuiStyleVar
import org.lwjgl.glfw.GLFW
import strongdmm.ui.dialog.edit_vars.model.Variable
import strongdmm.util.icons.ICON_FA_UNDO_ALT
import strongdmm.util.imgui.*
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 600f * Window.pointSize
        private val height: Float
            get() = 550f * Window.pointSize

        private val controlsFilterWidth: Float
            get() = ImGui.getWindowWidth() - (105f * Window.pointSize)
    }

    lateinit var viewController: ViewController

    fun process() {
        viewController.getTileItem()?.let { tileItem ->
            ImGuiUtil.setNextWindowCentered(width, height)

            imGuiBegin("Edit Variables: ${tileItem.type}##edit_variables_${state.windowId}") {
                showControls()

                ImGui.separator()

                imGuiChild("vars_table") {
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
            ImGui.setKeyboardFocusHere()
            state.isFistOpen = false
        }

        ImGui.setNextItemWidth(controlsFilterWidth)
        ImGuiExt.inputTextPlaceholder("##vars_filter", state.varsFilter, "Variables Filter")
        ImGui.sameLine()
        imGuiButton("OK", block = viewController::doOk)
        ImGui.sameLine()
        imGuiButton("Cancel", block = viewController::doCancel)

        ImGui.checkbox("Modified##is_show_modified_vars", state.isShowModifiedVars)
        ImGui.sameLine()
        ImGui.checkbox("By Type##is_show_vars_by_type", state.isShowVarsByType)

        if (state.isShowVarsByType.get()) {
            ImGui.setNextItemWidth(-1f)
            ImGuiExt.inputTextPlaceholder("##types_filter", state.typesFilter, "Types Filter")
        }
    }

    private fun showVariablesByType() {
        for ((type, variables) in state.variablesByType) {
            if (viewController.isFilteredOutType(type)) {
                continue
            }

            ImGui.text(type)
            ImGui.sameLine()
            ImGui.textDisabled("(${variables.size})")
            ImGui.columns(2, "variables_by_$type", true)
            variables.forEach(::showVariable)
            ImGui.columns(1)
        }
    }

    private fun showAllVariables() {
        if (state.pinnedVariables.isNotEmpty()) {
            ImGui.textColored(COLOR_GOLD, "Pinned")
            ImGui.columns(2, "pinned_edit_vars_columns", true)
            showPinnedVariables()
            ImGui.columns(1)
            ImGui.newLine()
            ImGui.textDisabled("Other")
        }

        ImGui.columns(2, "edit_vars_columns", true)
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

        ImGui.sameLine(0f, 15f)
        ImGui.alignTextToFramePadding()

        if (variable.isModified || variable.isChanged) {
            ImGui.textColored(COLOR_LIME, variable.name)
        } else {
            ImGui.text(variable.name)
        }

        ImGui.nextColumn()

        showResetToDefaultButton(variable)

        ImGui.sameLine()

        if (variable === state.currentEditVar) {
            showVariableEditField(variable)
        } else {
            showVariableValue(variable)
        }

        ImGui.nextColumn()
        ImGui.separator()
    }

    private fun showVariablePinOption(variable: Variable) {
        imGuiWithStyleVar(ImGuiStyleVar.FramePadding, .25f, .25f) {
            imGuiRadioButton("##variable_pin_${variable.hash}", variable.isPinned) {
                viewController.doPinVariable(variable)
            }
        }
    }

    private fun showResetToDefaultButton(variable: Variable) {
        val defaultValue = viewController.getDefaultVariableValue(variable)
        val isAlreadyDefault = variable.value.get() == defaultValue

        if (isAlreadyDefault) {
            ImGuiUtil.pushDisabledItem()
        }

        imGuiButton("$ICON_FA_UNDO_ALT##_variable_reset_${variable.hash}") {
            viewController.resetVariableToDefault(variable)
        }

        if (isAlreadyDefault) {
            ImGuiUtil.popDisabledItem()
        } else {
            ImGuiExt.setItemHoveredTooltip(viewController.getDefaultVariableValue(variable))
        }
    }

    private fun showVariableEditField(variable: Variable) {
        ImGui.setNextItemWidth(ImGui.getColumnWidth(-1))

        if (!state.variableInputFocused) {
            ImGui.setKeyboardFocusHere()
            state.variableInputFocused = true
        }

        ImGui.inputText("##variable_edit_${variable.hash}", variable.value)

        if (ImGui.isKeyPressed(GLFW.GLFW_KEY_ENTER) || ImGui.isKeyPressed(GLFW.GLFW_KEY_KP_ENTER) || ImGui.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            viewController.doStopEdit()
        }
    }

    private fun showVariableValue(variable: Variable) {
        ImGui.pushStyleColor(ImGuiCol.Button, 0)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImGui.getColorU32(ImGuiCol.ButtonHovered))
        ImGui.pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0f, 0f)

        imGuiButton("${variable.value}##variable_value_${variable.hash}", ImGui.getColumnWidth(-1)) {
            viewController.doStartEdit(variable)
        }

        if (ImGui.isItemHovered()) {
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand)
        }

        ImGui.popStyleVar()
        ImGui.popStyleColor(2)
    }
}
