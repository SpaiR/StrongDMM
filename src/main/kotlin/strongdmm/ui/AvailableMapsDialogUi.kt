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
import strongdmm.util.imgui.button
import strongdmm.util.imgui.child
import strongdmm.util.imgui.popupModal
import strongdmm.util.imgui.selectable
import strongdmm.util.inline.AbsPath
import strongdmm.util.inline.RelPath

class AvailableMapsDialogUi : EventSender, EventConsumer {
    private var isOpen: Boolean = false
    private var selectedMapPath: AbsPath? = null // to store an absolute path for currently selected map
    private var selectionStatus: RelPath = RelPath.NONE // to display a currently selected map (relative path)
    private val mapFilter: ImString = ImString().apply { inputData.isResizable = true }

    init {
        consumeEvent(Event.AvailableMapsDialogUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isOpen) {
            openPopup("Available Maps")
        }

        setNextWindowSize(600f, 285f, ImGuiCond.Once)

        popupModal("Available Maps") {
            text("Selected: ${selectionStatus.value}")
            setNextItemWidth(getWindowWidth() - 20)
            inputText("##map_filter", mapFilter)

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

            if (!isOpen) {
                closeCurrentPopup()
            }
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
        isOpen = false
        selectedMapPath = null
        selectionStatus = RelPath.NONE
        sendEvent(Event.CanvasController.Block(CanvasBlockStatus(false)))
    }

    private fun handleOpen() {
        isOpen = true
        sendEvent(Event.CanvasController.Block(CanvasBlockStatus(true)))
    }
}
