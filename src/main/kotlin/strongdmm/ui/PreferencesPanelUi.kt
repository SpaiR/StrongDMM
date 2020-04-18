package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import imgui.enums.ImGuiMouseButton
import imgui.enums.ImGuiMouseCursor
import imgui.enums.ImGuiStyleVar
import strongdmm.controller.preferences.MapSaveMode
import strongdmm.controller.preferences.NudgeMode
import strongdmm.controller.preferences.Preferences
import strongdmm.controller.preferences.Selectable
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerPreferencesController
import strongdmm.event.type.ui.TriggerPreferencesPanelUi
import strongdmm.util.imgui.combo
import strongdmm.util.imgui.popupModal
import strongdmm.util.imgui.selectable
import strongdmm.util.imgui.withStyleVar
import strongdmm.window.AppWindow

class PreferencesPanelUi : EventConsumer, EventSender {
    private var isDoOpen: Boolean = false
    private val isOpened: ImBool = ImBool(false)
    private var checkOpenStatus: Boolean = false

    private lateinit var providedPrefs: Preferences

    private val mapSaveModes: List<Selectable> = MapSaveMode.values().toList()
    private val nudgeModes: List<NudgeMode> = NudgeMode.values().toList()

    init {
        consumeEvent(Provider.PreferencesControllerPreferences::class.java, ::handleProviderPreferencesControllerPreferences)
        consumeEvent(TriggerPreferencesPanelUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isDoOpen) {
            checkOpenStatus = true
            openPopup("Preferences")
            sendEvent(Reaction.ApplicationBlockChanged(true))
            isDoOpen = false
        }

        setNextWindowSize(450f, 500f, AppWindow.defaultWindowCond)

        popupModal("Preferences", isOpened) {
            text("Save Options")
            separator()

            showSelectOption(
                "Map Save Format",
                "Controls the format used by the editor to save the map.",
                "##map_save_format", providedPrefs.mapSaveMode.toString(),
                mapSaveModes
            ) {
                providedPrefs.mapSaveMode = it as MapSaveMode
            }

            newLine()
            showToggleOption(
                "Sanitize Variables",
                "Enables sanitizing for variables which are declared on the map, but the same as default.",
                "##sanitize_variables", providedPrefs.sanitizeInitialVariables
            )

            newLine()
            showToggleOption(
                "Clean Unused Keys",
                "When enabled, content tile keys which are not used on the map will be removed.",
                "##clean_unused_keys", providedPrefs.cleanUnusedKeys
            )

            newLine()
            showSelectOption(
                "Nudge Mode",
                "Controls which variables will be changed during the nudge.",
                "##nudge_mode", providedPrefs.nudgeMode.toString(),
                nudgeModes
            ) {
                providedPrefs.nudgeMode = it as NudgeMode
            }
        }

        if (checkOpenStatus && !isOpened.get()) {
            checkOpenStatus = false
            sendEvent(Reaction.ApplicationBlockChanged(false))
        }
    }

    private inline fun showSelectOption(
        header: String,
        desc: String,
        selectLabel: String,
        selectPreview: String,
        selectVariants: List<Selectable>,
        action: (Selectable) -> Unit
    ) {
        textWrapped(header)
        pushTextWrapPos()
        textDisabled(desc)
        popTextWrapPos()
        combo(selectLabel, selectPreview) {
            selectVariants.forEach { mode ->
                selectable(mode.toString(), providedPrefs.mapSaveMode == mode) {
                    action(mode)
                    sendEvent(TriggerPreferencesController.SavePreferences())
                }
            }
        }
    }

    private fun showToggleOption(header: String, desc: String, checkboxLabel: String, active: ImBool) {
        textWrapped(header)
        withStyleVar(ImGuiStyleVar.FramePadding, 1f, 1f) {
            if (checkbox(checkboxLabel, active)) {
                sendEvent(TriggerPreferencesController.SavePreferences())
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
            active.set(!active.get())
            sendEvent(TriggerPreferencesController.SavePreferences())
        }
    }

    private fun handleProviderPreferencesControllerPreferences(event: Event<Preferences, Unit>) {
        providedPrefs = event.body
    }

    private fun handleOpen() {
        isOpened.set(true)
        isDoOpen = true
    }
}
