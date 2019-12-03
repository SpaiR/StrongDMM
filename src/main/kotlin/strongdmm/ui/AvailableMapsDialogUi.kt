package strongdmm.ui

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui.bullet
import imgui.ImGui.closeCurrentPopup
import imgui.ImGui.openPopup
import imgui.ImGui.sameLine
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.WindowFlag
import imgui.dsl_.*
import strongdmm.controller.canvas.CanvasBlockStatus
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
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

        setNextWindowSize(Vec2(600, 275), Cond.Once)

        popupModal("Available Maps", null, WindowFlag.NoResize.i) {
            text("Selected: ${selectionStatus.value}")

            child("available_maps_list", Vec2(580, 200), true, WindowFlag.HorizontalScrollbar.i) {
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
            button("Cancel", ::closePopup)
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
