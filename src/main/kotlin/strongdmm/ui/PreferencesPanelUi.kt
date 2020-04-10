package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import strongdmm.controller.preferences.MapSaveMode
import strongdmm.controller.preferences.Preferences
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerPreferencesController
import strongdmm.event.type.ui.TriggerPreferencesPanelUi
import strongdmm.util.imgui.popupModal
import strongdmm.util.imgui.combo
import strongdmm.window.AppWindow

class PreferencesPanelUi : EventConsumer, EventSender {
    private var isDoOpen: Boolean = false
    private val isOpened: ImBool = ImBool(false)
    private var checkOpenStatus: Boolean = false

    private lateinit var providedPrefs: Preferences

    private val mapSaveModes: Array<MapSaveMode> = MapSaveMode.values()

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
            text("Save options")
            separator()
            showMapSaveModes()
            newLine()
            showSanitizeInitialVariables()
            newLine()
            showCleanUnusedKeys()
        }

        if (checkOpenStatus && !isOpened.get()) {
            checkOpenStatus = false
            sendEvent(Reaction.ApplicationBlockChanged(false))
        }
    }

    private fun showMapSaveModes() {
        textWrapped("Map save format")
        pushTextWrapPos()
        textDisabled("Controls the format used by the editor to save the map.")
        popTextWrapPos()
        combo("", providedPrefs.mapSaveMode.name) {
            mapSaveModes.forEach { mode ->
                if (selectable(mode.name, providedPrefs.mapSaveMode == mode)) {
                    providedPrefs.mapSaveMode = mode
                    sendEvent(TriggerPreferencesController.SavePreferences())
                }
            }
        }
    }

    private fun showSanitizeInitialVariables() {
        textWrapped("Sanitize variables")
        if (checkbox("##sanitize_variables", providedPrefs.sanitizeInitialVariables)) {
            sendEvent(TriggerPreferencesController.SavePreferences())
        }
        sameLine()
        alignTextToFramePadding()
        pushTextWrapPos()
        textDisabled("Sanitize variables which are declared for the object on the map, but have the same value as in the environment.")
        popTextWrapPos()
    }

    private fun showCleanUnusedKeys() {
        textWrapped("Clean unused keys")
        if (checkbox("##clean_unused_keys", providedPrefs.cleanUnusedKeys)) {
            sendEvent(TriggerPreferencesController.SavePreferences())
        }
        sameLine()
        alignTextToFramePadding()
        pushTextWrapPos()
        textDisabled("Remove tile keys which are not used on the map.")
        popTextWrapPos()
    }

    private fun handleProviderPreferencesControllerPreferences(event: Event<Preferences, Unit>) {
        providedPrefs = event.body
    }

    private fun handleOpen() {
        isOpened.set(true)
        isDoOpen = true
    }
}
