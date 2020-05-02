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
        private const val WIDTH: Float = 400f
        private const val HEIGHT: Float = 450f
    }

    lateinit var viewController: ViewController

    fun process() {
        viewController.getTileItem()?.let { tileItem ->
            WindowUtil.setNextPosAndSizeCentered(WIDTH, HEIGHT)

            window("Edit Variables: ${tileItem.type}##edit_variables_${state.windowId}") {
                showControls()

                separator()

                child("vars_table") {
                    showVariables()
                }
            }
        }
    }

    private fun showControls() {
        checkbox("##is_show_modified_vars", state.isShowModifiedVars)
        setItemHoveredTooltip("Show modified variables")

        sameLine()

        if (state.isFistOpen) {
            setKeyboardFocusHere()
            state.isFistOpen = false
        }

        setNextItemWidth(getWindowWidth() - 130f)
        inputText("##vars_filter", state.varsFilter, "Variables Filter")
        sameLine()
        button("OK", block = viewController::doOk)
        sameLine()
        button("Cancel", block = viewController::doCancel)
    }

    private fun showVariables() {
        columns(2, "edit_vars_columns", true)

        for (variable in state.variables) {
            // Filtering when we need to show only modified vars
            if (state.isShowModifiedVars.get() && viewController.isNotModifiedVariable(variable)) {
                continue
            }

            // Filtering when 'filter input' is not empty
            if (state.varsFilter.length > 0 && viewController.isNotFilteredVariable(variable)) {
                continue
            }

            alignTextToFramePadding()

            if (variable.isModified || variable.isChanged) {
                textColored(0f, 1f, 0f, 1f, variable.name)
            } else {
                text(variable.name)
            }

            nextColumn()

            if (variable === state.currentEditVar) {
                showCurrentEditVariable(variable)
            } else {
                showVariable(variable)
            }

            nextColumn()
            separator()
        }
    }

    private fun showCurrentEditVariable(variable: Variable) {
        setNextItemWidth(getColumnWidth(-1))

        if (!state.variableInputFocused) {
            setKeyboardFocusHere()
            state.variableInputFocused = true
        }

        inputText("##${variable.name}", variable.value)

        if (isKeyPressed(GLFW.GLFW_KEY_ENTER) || isKeyPressed(GLFW.GLFW_KEY_KP_ENTER) || isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            viewController.doStopEdit()
        }
    }

    private fun showVariable(variable: Variable) {
        pushStyleColor(ImGuiCol.Button, 0)
        pushStyleColor(ImGuiCol.ButtonHovered, .25f, .58f, .98f, .5f)
        pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0f, 0f)

        button("${variable.value}##variable_btn_${variable.hash}", getColumnWidth(-1)) {
            viewController.doStartEdit(variable)
        }

        if (isItemHovered()) {
            setMouseCursor(ImGuiMouseCursor.Hand)
        }

        popStyleVar()
        popStyleColor(2)
    }
}
