package strongdmm.ui

import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW.*
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventMapHolderController
import strongdmm.event.type.ui.EventAvailableMapsDialogUi
import strongdmm.util.imgui.*
import strongdmm.window.AppWindow
import java.io.File

class AvailableMapsDialogUi : EventSender, EventConsumer {
    private var isDoOpen: Boolean = false
    private var isFirstOpen: Boolean = true

    private var providedAvailableMaps: Set<Pair<String, String>> = emptySet()
    private var selectedMapPath: String? = null // to store an absolute path for currently selected map
    private var selectionStatus: String = "" // to display a currently selected map (visible path)

    private val mapFilter: ImString = ImString().apply { inputData.isResizable = true }

    init {
        consumeEvent(EventAvailableMapsDialogUi.Open::class.java, ::handleOpen)
        consumeEvent(EventGlobalProvider.MapHolderControllerAvailableMaps::class.java, ::handleProviderMapHolderControllerAvailableMaps)
    }

    fun process() {
        if (isDoOpen) {
            openPopup("Available Maps")
            isDoOpen = false
        }

        setNextWindowSize(600f, 285f, AppWindow.defaultWindowCond)

        popupModal("Available Maps") {
            text("Selected: $selectionStatus")
            setNextItemWidth(getWindowWidth() - 20)

            if (isFirstOpen) {
                setKeyboardFocusHere()
                isFirstOpen = false
            }

            inputText("##maps_path_filter", mapFilter, "Paths Filter")

            child("available_maps_list", getWindowWidth() - 20, getWindowHeight() - 100, true, ImGuiWindowFlags.HorizontalScrollbar) {
                for ((absoluteFilePath, visibleFilePath) in providedAvailableMaps) {
                    if (mapFilter.length > 0 && !visibleFilePath.contains(mapFilter.get(), ignoreCase = true)) {
                        continue
                    }

                    bullet()
                    sameLine()
                    selectable(visibleFilePath, selectedMapPath == absoluteFilePath) {
                        selectedMapPath = absoluteFilePath
                        selectionStatus = visibleFilePath
                    }
                }
            }

            button("Open", block = ::openSelectedMapAndClosePopup)
            sameLine()
            button("Cancel", block = ::closePopup)

            if (isKeyPressed(GLFW_KEY_ENTER) || isKeyPressed(GLFW_KEY_KP_ENTER)) {
                openSelectedMapAndClosePopup()
            } else if (isKeyPressed(GLFW_KEY_ESCAPE)) {
                closePopup()
            }
        }
    }

    private fun openSelectedMapAndClosePopup() {
        selectedMapPath?.let {
            sendEvent(EventMapHolderController.OpenMap(File(it)))
            closePopup()
        }
    }

    private fun closePopup() {
        closeCurrentPopup()
        selectedMapPath = null
        selectionStatus = ""
        sendEvent(EventGlobal.ApplicationBlockChanged(false))
    }

    private fun handleOpen() {
        isDoOpen = true
        isFirstOpen = true
        sendEvent(EventGlobal.ApplicationBlockChanged(true))
    }

    private fun handleProviderMapHolderControllerAvailableMaps(event: Event<Set<Pair<String, String>>, Unit>) {
        providedAvailableMaps = event.body
    }
}
