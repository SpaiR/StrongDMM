package strongdmm.ui

import gnu.trove.map.hash.TObjectIntHashMap
import imgui.ImGui.*
import imgui.enums.ImGuiCol
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventMapHolderController
import strongdmm.util.imgui.RED32
import strongdmm.util.imgui.setItemHoveredTooltip
import strongdmm.util.imgui.smallButton
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class OpenedMapsPanelUi : EventConsumer, EventSender {
    private lateinit var providedOpenedMaps: Set<Dmm>
    private lateinit var providedActionBalanceStorage: TObjectIntHashMap<Dmm>

    private var selectedMap: Dmm? = null

    init {
        consumeEvent(EventGlobalProvider.MapHolderControllerOpenedMaps::class.java, ::handleProviderMapHolderControllerOpenedMaps)
        consumeEvent(EventGlobalProvider.ActionControllerActionBalanceStorage::class.java, ::handleProviderActionControllerActionBalanceStorage)
        consumeEvent(EventGlobal.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
    }

    fun process() {
        if (providedOpenedMaps.isEmpty() || selectedMap == null) {
            return
        }

        setNextWindowPos(AppWindow.windowWidth - 160f, 30f, AppWindow.defaultWindowCond)
        setNextWindowSize(150f, 150f, AppWindow.defaultWindowCond)

        window("${getMapName(selectedMap!!)}###opened_maps") {
            providedOpenedMaps.toTypedArray().forEach { map ->
                pushStyleColor(ImGuiCol.ButtonHovered, RED32)
                smallButton("X##close_map_${map.mapPath.readable}") {
                    sendEvent(EventMapHolderController.CloseMap(map.id))
                }
                popStyleColor()

                sameLine()

                if (selectable(getMapName(map), selectedMap === map)) {
                    if (selectedMap !== map) {
                        sendEvent(EventMapHolderController.ChangeSelectedMap(map.id))
                    }
                }
                setItemHoveredTooltip(map.mapPath.readable)
            }
        }
    }

    private fun getMapName(map: Dmm): String {
        return map.mapName + if (providedActionBalanceStorage[map] != 0) " *" else ""
    }

    private fun handleProviderMapHolderControllerOpenedMaps(event: Event<Set<Dmm>, Unit>) {
        providedOpenedMaps = event.body
    }

    private fun handleProviderActionControllerActionBalanceStorage(event: Event<TObjectIntHashMap<Dmm>, Unit>) {
        providedActionBalanceStorage = event.body
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        selectedMap = event.body
    }

    private fun handleOpenedMapClosed(event: Event<Dmm, Unit>) {
        if (event.body === selectedMap) {
            selectedMap = null
        }
    }
}
