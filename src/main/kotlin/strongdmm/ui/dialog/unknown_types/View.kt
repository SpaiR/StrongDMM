package strongdmm.ui.dialog.unknown_types

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiTreeNodeFlags
import strongdmm.service.map.UnknownType
import strongdmm.util.imgui.*
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 400 * Window.pointSize
        private val height: Float
            get() = 500 * Window.pointSize

        private const val TITLE: String = "Unknown Types"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.isDoOpen) {
            ImGui.openPopup(TITLE)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        showWindow()

        viewController.checkVariableToRemove()
        viewController.checkHasUnknownTypes()
    }

    private fun showWindow() {
        ImGuiUtil.setNextWindowCentered(width, height, ImGuiCond.Appearing)

        imGuiPopupModal(TITLE) {
            ImGui.text("Resolve all unknown types.")

            ImGui.sameLine()

            ImGuiExt.helpMark("""
                |You are trying to open a map with types unknown for your environment.
                |Please provide new types to use instead.
                |Keep a "New Type" field clear to delete unknown type completely.
                |You can modify instance variables as well.
                """.trimMargin()
            )

            ImGui.newLine()

            for ((index, unknownType) in state.unknownTypes.withIndex()) {
                ImGui.separator()

                if (ImGui.collapsingHeader(unknownType.originalTileObject.type, ImGuiTreeNodeFlags.DefaultOpen)) {
                    ImGui.pushID(index)

                    showNewTypeInput(unknownType)

                    ImGui.alignTextToFramePadding()
                    ImGui.text("Variables")

                    ImGui.sameLine()

                    imGuiButton(ImGuiIconFA.PLUS) {
                        viewController.doAddVariable(unknownType)
                    }

                    showVariables(unknownType)

                    ImGui.popID()
                }

                ImGui.separator()
                ImGui.newLine()
            }

            ImGui.newLine()

            showControlButtons()
        }
    }

    private fun showNewTypeInput(unknownType: UnknownType) {
        val textColor = if (viewController.isUnknownType(unknownType.type)) COLOR_RED else ImGui.getColorU32(ImGuiCol.Text)

        ImGui.alignTextToFramePadding()
        ImGui.textColored(textColor, "New Type")

        ImGui.sameLine()

        state.inputStr.set(unknownType.type)
        ImGui.setNextItemWidth(-1f)
        if (ImGui.inputText("##input_unknown_type", state.inputStr)) {
            viewController.doSetNewType(unknownType)
        }
    }

    private fun showVariables(unknownType: UnknownType) {
        if (unknownType.variables.isEmpty()) {
            return
        }

        imGuiChild("##variables", ImGui.getWindowWidth(), viewController.getVariablesHeight(unknownType), true) {
            ImGui.columns(2)

            unknownType.variables.forEachIndexed { index, variable ->
                ImGui.pushID(index)

                imGuiButton(ImGuiIconFA.TIMES) {
                    viewController.doRemoveVariable(unknownType, variable)
                }

                ImGui.sameLine()

                state.inputStr.set(variable.name)
                ImGui.setNextItemWidth(ImGui.getColumnWidth())
                if (ImGui.inputText("##variable_name", state.inputStr)) {
                    viewController.doSetVariableName(variable)
                }

                ImGui.nextColumn()

                state.inputStr.set(variable.value)
                ImGui.setNextItemWidth(ImGui.getColumnWidth())
                if (ImGui.inputText("##variable_value", state.inputStr)) {
                    viewController.doSetVariableValue(variable)
                }

                ImGui.separator()
                ImGui.nextColumn()

                ImGui.popID()
            }
        }
    }

    private fun showControlButtons() {
        if (state.hasUnknownTypes) {
            ImGuiUtil.pushDisabledItem()
        }

        imGuiButton("Continue", block = viewController::doContinue)

        if (state.hasUnknownTypes) {
            ImGuiUtil.popDisabledItem()
        }

        ImGui.sameLine()

        imGuiButton("Cancel", block = viewController::doCancel)
    }
}
