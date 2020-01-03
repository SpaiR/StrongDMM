package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiWindowFlags
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

    init {
        consumeEvent(Event.AvailableMapsDialogUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isOpen) {
            openPopup("Available Maps")
        }

        setNextWindowSize(600f, 275f, ImGuiCond.Once)

        popupModal("Available Maps", ImGuiWindowFlags.NoResize) {
            text("Selected: ${selectionStatus.value}")

            child("available_maps_list", 580f, 200f, true, ImGuiWindowFlags.HorizontalScrollbar) {
                sendEvent(Event.MapController.FetchAllAvailable { availableMaps ->
                    availableMaps.forEach { (abs, rel) ->
                        bullet()
                        sameLine()
                        selectable(rel.value, selectedMapPath == abs) {
                            selectedMapPath = abs
                            selectionStatus = rel
                        }
                    }
                })
            }

            button("OK") {
                sendEvent(Event.MapController.Open(selectedMapPath!!))
                closePopup()
            }
            sameLine()
            button("Cancel", block = ::closePopup)
        }
    }

    private fun closePopup() {
        closeCurrentPopup()
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
