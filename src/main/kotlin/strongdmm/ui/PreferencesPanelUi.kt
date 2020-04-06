package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import imgui.enums.ImGuiCond
import strongdmm.controller.preferences.MapSaveMode
import strongdmm.controller.preferences.Preferences
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventPreferencesController
import strongdmm.event.type.ui.EventPreferencesPanelUi
import strongdmm.util.imgui.helpMark
import strongdmm.util.imgui.popupModal
import strongdmm.util.imgui.withIndent

class PreferencesPanelUi : EventConsumer, EventSender {
    private var isDoOpen: Boolean = false
    private val isOpened: ImBool = ImBool(false)
    private var checkOpenStatus: Boolean = false

    private lateinit var providedPrefs: Preferences

    private val mapSaveModes: Array<MapSaveMode> = MapSaveMode.values()

    init {
        consumeEvent(EventGlobalProvider.PreferencesControllerPreferences::class.java, ::handleProviderPreferencesControllerPreferences)
        consumeEvent(EventPreferencesPanelUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isDoOpen) {
            checkOpenStatus = true
            openPopup("Preferences")
            sendEvent(EventGlobal.ApplicationBlockChanged(true))
            isDoOpen = false
        }

        setNextWindowSize(400f, 500f, ImGuiCond.Once)

        popupModal("Preferences", isOpened) {
            text("Save options")
            separator()
            showMapSaveModes()
            showSanitizeInitialVariables()
            showCleanUnusedKeys()
        }

        if (checkOpenStatus && !isOpened.get()) {
            checkOpenStatus = false
            sendEvent(EventGlobal.ApplicationBlockChanged(false))
        }
    }

    private fun showMapSaveModes() {
        bulletText("Map save format:")
        withIndent(20f) {
            mapSaveModes.forEach { mode ->
                if (radioButton(mode.name, providedPrefs.mapSaveMode == mode)) {
                    providedPrefs.mapSaveMode = mode
                    sendEvent(EventPreferencesController.SavePreferences())
                }
                sameLine()
                helpMark(mode.desc)
            }
        }
    }

    private fun showSanitizeInitialVariables() {
        bullet()
        sameLine()
        textWrapped("Sanitize variables:")
        withIndent(20f) {
            val label = getCheckboxStatusLabel(providedPrefs.sanitizeInitialVariables.get())
            if (checkbox("$label##sanitize_variables", providedPrefs.sanitizeInitialVariables)) {
                sendEvent(EventPreferencesController.SavePreferences())
            }
            sameLine()
            helpMark("Sanitize variables which are declared for the object on the map, but have the same value as in the environment.")
        }
    }

    private fun showCleanUnusedKeys() {
        bullet()
        sameLine()
        textWrapped("Clean unused keys:")
        withIndent(20f) {
            val label = getCheckboxStatusLabel(providedPrefs.cleanUnusedKeys.get())
            if (checkbox("$label##clean_unused_keys", providedPrefs.cleanUnusedKeys)) {
                sendEvent(EventPreferencesController.SavePreferences())
            }
            sameLine()
            helpMark("Remove tile keys which are not used on the map.")
        }
    }

    private fun getCheckboxStatusLabel(enabled: Boolean): String = if (enabled) "Enabled" else "Disabled"

    private fun handleProviderPreferencesControllerPreferences(event: Event<Preferences, Unit>) {
        providedPrefs = event.body
    }

    private fun handleOpen() {
        isOpened.set(true)
        isDoOpen = true
    }
}
