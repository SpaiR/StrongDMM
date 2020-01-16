package strongdmm.ui

import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
import org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER
import strongdmm.controller.canvas.CanvasBlockStatus
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.imgui.*
import strongdmm.util.inline.AbsPath
import strongdmm.util.inline.RelPath

class AvailableMapsDialogUi : EventSender, EventConsumer {
    private var isDoOpen: Boolean = false
    private var isFirstOpen: Boolean = true

    private var selectedMapPath: AbsPath? = null // to store an absolute path for currently selected map
    private var selectionStatus: RelPath = RelPath.NONE // to display a currently selected map (relative path)

    private val mapFilter: ImString = ImString().apply { inputData.isResizable = true }

    init {
        consumeEvent(Event.AvailableMapsDialogUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isDoOpen) {
            openPopup("Available Maps")
            isDoOpen = false
        }

        setNextWindowSize(600f, 285f, ImGuiCond.Once)

        popupModal("Available Maps") {
            text("Selected: ${selectionStatus.value}")
            setNextItemWidth(getWindowWidth() - 20)

            if (isFirstOpen) {
                setKeyboardFocusHere()
                isFirstOpen = false
            }

            inputText("##maps_path_filter", mapFilter, "Paths Filter")

            child("available_maps_list", getWindowWidth() - 20, getWindowHeight() - 100, true, ImGuiWindowFlags.HorizontalScrollbar) {
                sendEvent(Event.MapController.FetchAllAvailable { availableMaps ->
                    for ((abs, rel) in availableMaps) {
                        if (mapFilter.length > 0 && !rel.value.contains(mapFilter.get())) {
                            continue
                        }

                        bullet()
                        sameLine()
                        selectable(rel.value, selectedMapPath == abs) {
                            selectedMapPath = abs
                            selectionStatus = rel
                        }
                    }
                })
            }

            button("Open", block = ::openSelectedMapAndClosePopup)
            sameLine()
            button("Cancel", block = ::closePopup)
        }

        if (isKeyPressed(GLFW_KEY_ENTER) || isKeyPressed(GLFW_KEY_KP_ENTER)) {
            openSelectedMapAndClosePopup()
        }
    }

    private fun openSelectedMapAndClosePopup() {
        selectedMapPath?.let {
            sendEvent(Event.MapController.Open(it))
            closePopup()
        }
    }

    private fun closePopup() {
        closeCurrentPopup()
        selectedMapPath = null
        selectionStatus = RelPath.NONE
        sendEvent(Event.CanvasController.Block(CanvasBlockStatus(false)))
    }

    private fun handleOpen() {
        isDoOpen = true
        isFirstOpen = true
        sendEvent(Event.CanvasController.Block(CanvasBlockStatus(true)))
    }
}
