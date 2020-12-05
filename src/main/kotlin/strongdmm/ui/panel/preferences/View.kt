package strongdmm.ui.panel.preferences

import imgui.ImGui
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiMouseCursor
import imgui.flag.ImGuiStyleVar
import imgui.type.ImInt
import strongdmm.service.preferences.prefs.PreferenceBoolean
import strongdmm.service.preferences.prefs.PreferenceEnum
import strongdmm.service.preferences.prefs.PreferenceInteger
import strongdmm.util.imgui.*
import strongdmm.util.imgui.markdown.ImGuiMarkdown
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 600 * Window.pointSize
        private val height: Float
            get() = 800 * Window.pointSize

        private const val TITLE: String = "Preferences"

        private val toggleButtonPadding: Float
            get() = Window.pointSize

        private val optionsIndent: Float
            get() = 10f * Window.pointSize
    }

    lateinit var viewController: ViewController

    private val imInt: ImInt = ImInt()

    fun process() {
        if (state.isDoOpen) {
            state.checkOpenStatus = true
            ImGui.openPopup(TITLE)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        ImGuiUtil.setNextWindowCentered(width, height, ImGuiCond.Appearing)

        imGuiPopupModal(TITLE, state.isOpened) {
            state.preferencesByGroups.forEach { (groupName, preferences) ->
                ImGuiMarkdown.renderHeader(groupName)

                imGuiWithIndent(optionsIndent) {
                    preferences.forEach { pref ->
                        when (pref) {
                            is PreferenceInteger -> showInputIntOption(pref)
                            is PreferenceEnum -> showSelectOption(pref)
                            is PreferenceBoolean -> showToggleOption(pref)
                        }

                        ImGui.newLine()
                    }
                }
            }
        }

        viewController.checkOpenStatus()
    }

    private fun showInputIntOption(pref: PreferenceInteger) {
        ImGui.textWrapped(pref.getHeader())

        ImGui.pushTextWrapPos()
        ImGui.textDisabled(pref.getDesc())
        ImGui.popTextWrapPos()

        imInt.set(pref.getValue().data)
        if (ImGuiExt.inputIntClamp(pref.getLabel(), imInt, pref.getMin(), pref.getMax(), pref.getStep(), pref.getStepFast())) {
            pref.getValue().data = imInt.get()
            viewController.savePreferences()
        }
    }

    private fun showSelectOption(pref: PreferenceEnum) {
        ImGui.textWrapped(pref.getHeader())

        ImGui.pushTextWrapPos()
        ImGui.textDisabled(pref.getDesc())
        ImGui.popTextWrapPos()

        imGuiCombo(pref.getLabel(), pref.getReadableName()) {
            pref.getEnums().forEach { mode ->
                imGuiSelectable(mode.getReadableName(), pref == mode) {
                    viewController.doSelectOption(mode, pref)
                }
            }
        }
    }

    private fun showToggleOption(pref: PreferenceBoolean) {
        ImGui.textWrapped(pref.getHeader())

        imGuiWithStyleVar(ImGuiStyleVar.FramePadding, toggleButtonPadding, toggleButtonPadding) {
            if (ImGui.checkbox(pref.getLabel(), pref.getValue().data)) {
                viewController.doToggleOption(pref)
            }
        }

        ImGui.sameLine()

        ImGui.pushTextWrapPos()
        ImGui.textDisabled(pref.getDesc())
        ImGui.popTextWrapPos()

        if (ImGui.isItemHovered()) {
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand)
        }

        if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
            viewController.doToggleOption(pref)
        }
    }
}
