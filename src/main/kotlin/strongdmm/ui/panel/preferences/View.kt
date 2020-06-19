package strongdmm.ui.panel.preferences

import imgui.ImBool
import imgui.ImGui.*
import imgui.enums.ImGuiMouseButton
import imgui.enums.ImGuiMouseCursor
import imgui.enums.ImGuiStyleVar
import strongdmm.service.preferences.MapSaveMode
import strongdmm.service.preferences.NudgeMode
import strongdmm.service.preferences.Selectable
import strongdmm.util.imgui.*

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 450f
        private const val HEIGHT: Float = 500f

        private const val TITLE: String = "Preferences"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.isDoOpen) {
            state.checkOpenStatus = true
            openPopup(TITLE)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        ImGuiUtil.setNextSize(WIDTH, HEIGHT)

        popupModal(TITLE, state.isOpened) {
            text("Save Options")

            separator()

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

        viewController.checkOpenStatus()
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

        withStyleVar(ImGuiStyleVar.FramePadding, 1f, 1f) {
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
