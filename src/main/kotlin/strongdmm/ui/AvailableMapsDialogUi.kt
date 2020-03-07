package strongdmm.ui

import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW.*
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.imgui.*
import java.io.File

class AvailableMapsDialogUi : EventSender, EventConsumer {
    private var isDoOpen: Boolean = false
    private var isFirstOpen: Boolean = true

    private var availableMaps: Set<Pair<String, String>> = emptySet()
    private var selectedMapPath: String? = null // to store an absolute path for currently selected map
    private var selectionStatus: String = "" // to display a currently selected map (visible path)

    private val mapFilter: ImString = ImString().apply { inputData.isResizable = true }

    init {
        consumeEvent(Event.AvailableMapsDialogUi.Open::class.java, ::handleOpen)
        consumeEvent(Event.Global.Provider.AvailableMaps::class.java, ::handleProviderAvailableMaps)
    }

    fun process() {
        if (isDoOpen) {
            openPopup("Available Maps")
            isDoOpen = false
        }

        setNextWindowSize(600f, 285f, ImGuiCond.Once)

        popupModal("Available Maps") {
            text("Selected: $selectionStatus")
            setNextItemWidth(getWindowWidth() - 20)

            if (isFirstOpen) {
                setKeyboardFocusHere()
                isFirstOpen = false
            }

            inputText("##maps_path_filter", mapFilter, "Paths Filter")

            child("available_maps_list", getWindowWidth() - 20, getWindowHeight() - 100, true, ImGuiWindowFlags.HorizontalScrollbar) {
                for ((absoluteFilePath, visibleFilePath) in availableMaps) {
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
            sendEvent(Event.MapHolderController.Open(File(it)))
            closePopup()
        }
    }

    private fun closePopup() {
        closeCurrentPopup()
        selectedMapPath = null
        selectionStatus = ""
        sendEvent(Event.CanvasController.Block(false))
    }

    private fun handleOpen() {
        isDoOpen = true
        isFirstOpen = true
        sendEvent(Event.CanvasController.Block(true))
    }

    private fun handleProviderAvailableMaps(event: Event<Set<Pair<String, String>>, Unit>) {
        availableMaps = event.body
    }
}
