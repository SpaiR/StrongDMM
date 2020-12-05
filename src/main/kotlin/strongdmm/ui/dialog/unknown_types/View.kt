package strongdmm.ui.dialog.unknown_types

import imgui.ImGui.*
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiTreeNodeFlags
import strongdmm.service.map.UnknownType
import strongdmm.util.icons.ICON_FA_PLUS
import strongdmm.util.icons.ICON_FA_TIMES
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
            openPopup(TITLE)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        showWindow()

        viewController.checkVariableToRemove()
        viewController.checkHasUnknownTypes()
    }

    private fun showWindow() {
        ImGuiUtil.setNextWindowCentered(width, height, ImGuiCond.Appearing)

        popupModal(TITLE) {
            text("Resolve all unknown types.")

            sameLine()

            ImGuiExt.helpMark("""
                |You are trying to open a map with types unknown for your environment.
                |Please provide new types to use instead.
                |Keep a "New Type" field clear to delete unknown type completely.
                |You can modify instance variables as well.
                """.trimMargin()
            )

            newLine()

            for ((index, unknownType) in state.unknownTypes.withIndex()) {
                separator()

                if (collapsingHeader(unknownType.originalTileObject.type, ImGuiTreeNodeFlags.DefaultOpen)) {
                    pushID(index)

                    showNewTypeInput(unknownType)

                    alignTextToFramePadding()
                    text("Variables")

                    sameLine()

                    button(ICON_FA_PLUS) {
                        viewController.doAddVariable(unknownType)
                    }

                    showVariables(unknownType)

                    popID()
                }

                separator()
                newLine()
            }

            newLine()

            showControlButtons()
        }
    }

    private fun showNewTypeInput(unknownType: UnknownType) {
        val textColor = if (viewController.isUnknownType(unknownType.type)) COLOR_RED else getColorU32(ImGuiCol.Text)

        alignTextToFramePadding()
        textColored(textColor, "New Type")

        sameLine()

        state.inputStr.set(unknownType.type)
        setNextItemWidth(-1f)
        if (inputText("##input_unknown_type", state.inputStr)) {
            viewController.doSetNewType(unknownType)
        }
    }

    private fun showVariables(unknownType: UnknownType) {
        if (unknownType.variables.isEmpty()) {
            return
        }

        child("##variables", getWindowWidth(), viewController.getVariablesHeight(unknownType), true) {
            columns(2)

            unknownType.variables.forEachIndexed { index, variable ->
                pushID(index)

                button(ICON_FA_TIMES) {
                    viewController.doRemoveVariable(unknownType, variable)
                }

                sameLine()

                state.inputStr.set(variable.name)
                setNextItemWidth(getColumnWidth())
                if (inputText("##variable_name", state.inputStr)) {
                    viewController.doSetVariableName(variable)
                }

                nextColumn()

                state.inputStr.set(variable.value)
                setNextItemWidth(getColumnWidth())
                if (inputText("##variable_value", state.inputStr)) {
                    viewController.doSetVariableValue(variable)
                }

                separator()
                nextColumn()

                popID()
            }
        }
    }

    private fun showControlButtons() {
        if (state.hasUnknownTypes) {
            ImGuiUtil.pushDisabledButtonStyle()
        }

        button("Continue", block = viewController::doContinue)

        if (state.hasUnknownTypes) {
            ImGuiUtil.popDisabledButtonStyle()
        }

        sameLine()

        button("Cancel", block = viewController::doCancel)
    }
}
