package strongdmm.controller

import imgui.ImGui
import strongdmm.event.Event
import strongdmm.event.EventSender
import strongdmm.util.LMB

class InputMouseController : EventSender {
    fun process() {
        handleMouseDragging()
        handleZooming()
    }

    private fun handleMouseDragging() {
        if (ImGui.isMouseDown(LMB)) {
            if (ImGui.io.mouseDelta anyNotEqual 0f) {
                sendEvent(Event.CANVAS_VIEW_TRANSLATE, ImGui.io.mouseDelta)
            }
        }
    }

    private fun handleZooming() {
        if (ImGui.io.mouseWheel != 0f) {
            sendEvent(Event.CANVAS_VIEW_SCALE, ImGui.io.mouseWheel > 0)
        }
    }
}
