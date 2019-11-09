package strongdmm.ui

import glm_.vec2.Vec2
import imgui.Col
import imgui.Cond
import imgui.ImGui.isItemHovered
import imgui.ImGui.sameLine
import imgui.ImGui.selectable
import imgui.ImGui.setNextWindowCollapsed
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.dsl.smallButton
import imgui.dsl.tooltip
import imgui.dsl.window
import imgui.dsl.withStyleColor
import strongdmm.event.Event
import strongdmm.event.EventSender
import strongdmm.util.RED32

class OpenedMapsPanelUi : Window(), EventSender {
    fun process(windowWidth: Int, windowHeight: Int) {
        sendEvent(Event.MapController.FetchOpened { openedMaps ->
            if (openedMaps.isEmpty()) {
                return@FetchOpened
            }

            getOptionCondition(windowWidth, windowHeight).let { cond ->
                setNextWindowPos(Vec2(windowWidth - 160, 30), cond)
                setNextWindowSize(Vec2(150, 150), cond)
                setNextWindowCollapsed(true, Cond.Once)
            }

            sendEvent(Event.MapController.FetchSelected { selectedMap ->
                window("${selectedMap?.mapName}###opened_maps") {
                    openedMaps.forEach { map ->
                        withStyleColor(Col.ButtonHovered, RED32) {
                            smallButton("X##close_map_${map.relativeMapPath}") {
                                sendEvent(Event.MapController.Close(map.id))
                            }
                        }

                        sameLine()

                        if (selectable(map.mapName, selectedMap == map) && selectedMap != map) {
                            sendEvent(Event.MapController.Switch(map.id))
                        }

                        if (isItemHovered()) {
                            tooltip {
                                text(map.relativeMapPath)
                            }
                        }
                    }
                }
            })
        })
    }
}
