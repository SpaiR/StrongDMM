package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiCond
import strongdmm.event.Event
import strongdmm.event.EventSender
import strongdmm.util.imgui.*

class OpenedMapsPanelUi : EventSender {
    fun process(windowWidth: Int) {
        sendEvent(Event.MapController.FetchAllOpened { openedMaps ->
            if (openedMaps.isEmpty()) {
                return@FetchAllOpened
            }

            setNextWindowPos(windowWidth - 160f, 30f, ImGuiCond.Once)
            setNextWindowSize(150f, 150f, ImGuiCond.Once)
            setNextWindowCollapsed(true, ImGuiCond.Once)

            sendEvent(Event.MapController.FetchSelected { selectedMap ->
                window("${selectedMap?.mapName}###opened_maps") {
                    openedMaps.forEach { map ->
                        pushStyleColor(ImGuiCol.ButtonHovered, RED32)
                        smallButton("X##close_map_${map.relMapPath}") {
                            sendEvent(Event.MapController.Close(map.id))
                        }
                        popStyleColor()

                        sameLine()

                        selectable(map.mapName, selectedMap == map).itemAction {
                            if (selectedMap != map) {
                                sendEvent(Event.MapController.Switch(map.id))
                            }
                        }.itemHovered {
                            setTooltip(map.relMapPath.value)
                        }
                    }
                }
            })
        })
    }
}
