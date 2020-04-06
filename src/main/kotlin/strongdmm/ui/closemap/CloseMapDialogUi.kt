package strongdmm.ui.closemap

import imgui.ImGui.*
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.controller.EventCanvasController
import strongdmm.event.type.ui.EventCloseMapDialogUi
import strongdmm.util.imgui.button
import strongdmm.util.imgui.popupModal

class CloseMapDialogUi : EventSender, EventConsumer {
    private var isDoOpen: Boolean = false

    private var eventToReply: Event<Dmm, CloseMapStatus>? = null

    init {
        consumeEvent(EventCloseMapDialogUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isDoOpen) {
            openPopup("Save Map?##close_map_dialog")
            sendEvent(EventCanvasController.BlockCanvas(true))
            isDoOpen = false
        }

        setNextWindowSize(350f, 100f, ImGuiCond.Once)

        popupModal("Save Map?##close_map_dialog", ImGuiWindowFlags.NoResize) {
            text("Map ${eventToReply?.body?.mapName} has been modified. Save changes?")
            newLine()
            button("Yes") { closeDialog(CloseMapStatus.SAVE) }
            sameLine()
            button("No") { closeDialog(CloseMapStatus.CLOSE) }
            sameLine()
            button("Cancel") { closeDialog(CloseMapStatus.CANCEL) }
        }
    }

    private fun closeDialog(status: CloseMapStatus) {
        closeCurrentPopup()
        sendEvent(EventCanvasController.BlockCanvas(false))
        eventToReply?.reply(status)
        eventToReply = null
    }

    private fun handleOpen(event: Event<Dmm, CloseMapStatus>) {
        isDoOpen = true
        eventToReply = event
    }
}
