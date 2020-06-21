package strongdmm.ui.panel.preferences

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImInt
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiMouseButton
import imgui.enums.ImGuiMouseCursor
import imgui.enums.ImGuiStyleVar
import strongdmm.service.preferences.model.MapSaveMode
import strongdmm.service.preferences.model.NudgeMode
import strongdmm.service.preferences.model.Selectable
import strongdmm.util.imgui.*
import strongdmm.util.imgui.inputIntClamp
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 450f * Window.pointSize
        private val height: Float
            get() = 500f * Window.pointSize

        private const val TITLE: String = "Preferences"

        private val toggleButtonPadding: Float
            get() = Window.pointSize

        private val optionsIndent: Float
            get() = 10f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.isDoOpen) {
            state.checkOpenStatus = true
            openPopup(TITLE)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        ImGuiUtil.setNextWindowCentered(width, height, ImGuiCond.Appearing)

        popupModal(TITLE, state.isOpened) {
            textColored(1f, .84f, 0f, .75f, "Interface Options")
            separator()

            withIndent(optionsIndent) {
                showInputIntOption(
                    "Scale",
                    "Controls the interface scale.",
                    "Scale Percent",
                    state.providedPreferences.interfaceScalePercent,
                    50,
                    250,
                    1,
                    10
                )
            }

            newLine()

            textColored(1f, .84f, 0f, .75f, "Save Options")
            separator()

            withIndent(optionsIndent) {
                showSelectOption(
                    "Map Save Format",
                    "Controls the format used by the editor to save the map.",
                    "##map_save_format", state.providedPreferences.mapSaveMode.toString(),
                    state.mapSaveModes,
                    state.providedPreferences.mapSaveMode
                ) {
                    state.providedPreferences.mapSaveMode = it as MapSaveMode
                }

                newLine()

                showToggleOption(
                    "Sanitize Variables",
                    "Enables sanitizing for variables which are declared on the map, but the same as default.",
                    "##sanitize_variables", state.providedPreferences.sanitizeInitialVariables
                )

                newLine()

                showToggleOption(
                    "Clean Unused Keys",
                    "When enabled, content tile keys which are not used on the map will be removed.",
                    "##clean_unused_keys", state.providedPreferences.cleanUnusedKeys
                )

                newLine()

                showSelectOption(
                    "Nudge Mode",
                    "Controls which variables will be changed during the nudge.",
                    "##nudge_mode", state.providedPreferences.nudgeMode.toString(),
                    state.nudgeModes,
                    state.providedPreferences.nudgeMode
                ) {
                    state.providedPreferences.nudgeMode = it as NudgeMode
                }
            }
        }

        viewController.checkOpenStatus()
    }

    private fun showInputIntOption(header: String, desc: String, inputLabel: String, option: ImInt, min: Int, max: Int, step: Int, stepFast: Int) {
        textWrapped(header)

        pushTextWrapPos()
        textDisabled(desc)
        popTextWrapPos()

        if (inputIntClamp(inputLabel, option, min, max, step, stepFast)) {
            viewController.savePreferences()
        }
    }

    private inline fun showSelectOption(
        header: String,
        desc: String,
        selectLabel: String,
        selectPreview: String,
        selectVariants: List<Selectable>,
        selectedVariant: Selectable,
        action: (Selectable) -> Unit
    ) {
        textWrapped(header)

        pushTextWrapPos()
        textDisabled(desc)
        popTextWrapPos()

        combo(selectLabel, selectPreview) {
            selectVariants.forEach { mode ->
                selectable(mode.toString(), selectedVariant == mode) {
                    viewController.doSelectOption(mode, action)
                }
            }
        }
    }

    private fun showToggleOption(header: String, desc: String, checkboxLabel: String, option: ImBool) {
        textWrapped(header)

        withStyleVar(ImGuiStyleVar.FramePadding, toggleButtonPadding, toggleButtonPadding) {
            if (checkbox(checkboxLabel, option)) {
                viewController.savePreferences()
            }
        }

        sameLine()

        pushTextWrapPos()
        textDisabled(desc)
        popTextWrapPos()

        if (isItemHovered()) {
            setMouseCursor(ImGuiMouseCursor.Hand)
        }

        if (isItemClicked(ImGuiMouseButton.Left)) {
            viewController.doToggleOption(option)
        }
    }
}
