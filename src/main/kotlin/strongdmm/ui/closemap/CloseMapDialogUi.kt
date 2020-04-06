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

    private var eventToReply: Event<Dmm, CloseMapDialogStatus>? = null

    init {
        consumeEvent(EventCloseMapDialogUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isDoOpen) {
            openPopup("Save Map?##close_map_dialog")
            sendEvent(EventCanvasController.BlockCanvas(true))
            isDoOpen = false
        }

        setNextWindowSize(400f, 100f, ImGuiCond.Once)

        popupModal("Save Map?##close_map_dialog", ImGuiWindowFlags.NoResize) {
            text("Map ${eventToReply?.body?.mapName} has been modified. Save changes?")
            newLine()
            button("Yes") { closeDialog(CloseMapDialogStatus.CLOSE_WITH_SAVE) }
            sameLine()
            button("No") { closeDialog(CloseMapDialogStatus.CLOSE) }
            sameLine()
            button("Cancel") { closeDialog(CloseMapDialogStatus.CANCEL) }
        }
    }

    private fun closeDialog(status: CloseMapDialogStatus) {
        closeCurrentPopup()
        sendEvent(EventCanvasController.BlockCanvas(false))
        eventToReply?.reply(status)
        eventToReply = null
    }

    private fun handleOpen(event: Event<Dmm, CloseMapDialogStatus>) {
        isDoOpen = true
        eventToReply = event
    }
}
