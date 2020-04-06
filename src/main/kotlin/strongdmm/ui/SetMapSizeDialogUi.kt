package strongdmm.ui

import imgui.ImGui.*
import imgui.ImInt
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.dmm.MapSize
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.EventMapHolderController
import strongdmm.event.type.controller.EventMapModifierController
import strongdmm.event.type.ui.EventSetMapSizeDialogUi
import strongdmm.util.imgui.button
import strongdmm.util.imgui.popupModal
import strongdmm.window.AppWindow

class SetMapSizeDialogUi : EventConsumer, EventSender {
    private var isDoOpen: Boolean = false

    private val newX: ImInt = ImInt(0)
    private val newY: ImInt = ImInt(0)
    private val newZ: ImInt = ImInt(0)

    init {
        consumeEvent(EventSetMapSizeDialogUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (isDoOpen) {
            openPopup("Set Map Size")
            sendEvent(EventGlobal.ApplicationBlockChanged(true))
            isDoOpen = false
        }

        setNextWindowSize(295f, 100f, AppWindow.defaultWindowCond)

        popupModal("Set Map Size", ImGuiWindowFlags.NoResize) {
            setNextItemWidth(75f)
            showInput("X", newX)
            sameLine()
            setNextItemWidth(75f)
            showInput("Y", newY)
            sameLine()
            setNextItemWidth(75f)
            showInput("Z", newZ)

            newLine()
            button("OK") {
                sendEvent(EventMapModifierController.ChangeMapSize(MapSize(newX.get(), newY.get(), newZ.get())))
                closeDialog()
            }
            sameLine()
            button("Cancel") {
                closeDialog()
            }
        }
    }

    private fun showInput(label: String, data: ImInt) {
        if (inputInt(label, data)) {
            if (data.get() <= 0) {
                data.set(1)
            }
        }
    }

    private fun closeDialog() {
        closeCurrentPopup()
        sendEvent(EventGlobal.ApplicationBlockChanged(false))
    }

    private fun handleOpen() {
        sendEvent(EventMapHolderController.FetchSelectedMap {
            newX.set(it.maxX)
            newY.set(it.maxY)
            newZ.set(it.maxZ)

            isDoOpen = true
        })
    }
}
