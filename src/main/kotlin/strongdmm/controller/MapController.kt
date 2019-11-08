package strongdmm.controller

import io.github.spair.dmm.io.reader.DmmReader
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.Message
import java.io.File

class MapController : EventSender, EventConsumer {
    private val openedMaps: MutableSet<Dmm> = mutableSetOf()
    private var selectedMap: Dmm? = null

    init {
        consumeEvent(Event.MAP_OPEN, ::handleOpen)
        consumeEvent(Event.MAP_CLOSE, ::handleClose)
        consumeEvent(Event.MAP_FETCH_SELECTED, ::handleFetchSelected)
        consumeEvent(Event.MAP_FETCH_OPENED, ::handleFetchOpened)
        consumeEvent(Event.MAP_SWITCH, ::handleSwitch)
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
    }

    private fun handleOpen(msg: Message<String, Unit>) {
        if (selectedMap?.relativeMapPath == msg.body) {
            return
        }

        val dmm = openedMaps.find { it.relativeMapPath == msg.body }

        if (dmm != null) {
            selectedMap = dmm
            sendEvent(Event.GLOBAL_SWITCH_MAP, dmm)
        } else {
            val mapFile = File(msg.body)

            if (!mapFile.isFile) {
                return
            }

            sendEvent<Dme>(Event.ENVIRONMENT_FETCH) { environment ->
                val dmmData = DmmReader.readMap(mapFile)
                val map = Dmm(mapFile, dmmData, environment)
                openedMaps.add(map)
                selectedMap = map
                sendEvent(Event.GLOBAL_SWITCH_MAP, map)
            }
        }
    }

    private fun handleClose(msg: Message<String, Unit>) {
        openedMaps.find { it.relativeMapPath == msg.body }?.let {
            val mapIndex = openedMaps.indexOf(it)

            openedMaps.remove(it)
            sendEvent(Event.GLOBAL_CLOSE_MAP, it)

            if (selectedMap === it) {
                if (openedMaps.isEmpty()) {
                    selectedMap = null
                } else {
                    val index = if (mapIndex == openedMaps.size) mapIndex - 1 else mapIndex
                    selectedMap = openedMaps.toList()[index]
                    sendEvent(Event.GLOBAL_SWITCH_MAP, selectedMap)
                }
            }
        }
    }

    private fun handleFetchSelected(msg: Message<Unit, Dmm?>) {
        msg.reply(selectedMap)
    }

    private fun handleFetchOpened(msg: Message<Unit, Set<Dmm>>) {
        msg.reply(openedMaps.toSet())
    }

    private fun handleSwitch(msg: Message<String, Unit>) {
        openedMaps.find { it.relativeMapPath == msg.body }?.let {
            if (selectedMap !== it) {
                selectedMap = it
                sendEvent(Event.GLOBAL_SWITCH_MAP, it)
            }
        }
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        selectedMap = null
        openedMaps.clear()
    }
}
