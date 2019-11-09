package strongdmm.ui

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui.bullet
import imgui.ImGui.closeCurrentPopup
import imgui.ImGui.openPopup
import imgui.ImGui.sameLine
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.dsl_.*
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import imgui.WindowFlag as Wf

class AvailableMapsDialogUi : EventSender, EventConsumer {
    private var isOpen: Boolean = false
    private var selectedMapPath: String? = null // to store absolute path for currently selected map
    private var selectionStatus: String = "no map" // to display currently selected map (relative path)

    init {
        consumeEvent(Event.AvailableMaps.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isOpen) {
            openPopup("Available Maps")
        }

        setNextWindowSize(Vec2(600, 275), Cond.Once)

        popupModal("Available Maps", null, Wf.NoResize.i) {
            text("Selected: $selectionStatus")

            child("available_maps_list", Vec2(580, 200), true, Wf.HorizontalScrollbar.i) {
                sendEvent(Event.Map.FetchAvailable { availableMaps ->
                    availableMaps.forEach { (abs, rel) ->
                        bullet()
                        sameLine()
                        selectable(rel, selectedMapPath == abs) {
                            selectedMapPath = abs
                            selectionStatus = rel
                        }
                    }
                })
            }

            button("OK") {
                sendEvent(Event.Map.Open(selectedMapPath!!))
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
        selectionStatus = "no map"
    }

    private fun handleOpen() {
        isOpen = true
    }
}
