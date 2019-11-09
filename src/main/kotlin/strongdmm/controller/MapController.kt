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
    private val availableMaps: MutableSet<Pair<String, String>> = mutableSetOf()

    private var selectedMap: Dmm? = null

    init {
        consumeEvent(Event.MAP_OPEN, ::handleOpen)
        consumeEvent(Event.MAP_CLOSE, ::handleClose)
        consumeEvent(Event.MAP_FETCH_SELECTED, ::handleFetchSelected)
        consumeEvent(Event.MAP_FETCH_OPENED, ::handleFetchOpened)
        consumeEvent(Event.MAP_FETCH_AVAILABLE, ::handleFetchAvailable)
        consumeEvent(Event.MAP_SWITCH, ::handleSwitch)
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
        consumeEvent(Event.GLOBAL_SWITCH_ENVIRONMENT, ::handleSwitchEnvironment)
    }

    private fun handleOpen(msg: Message<String, Unit>) {
        val id = msg.body.hashCode()

        if (selectedMap?.id == id) {
            return
        }

        val dmm = openedMaps.find { it.id == id }

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

    private fun handleClose(msg: Message<Int, Unit>) {
        openedMaps.find { it.id == msg.body }?.let {
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

    private fun handleFetchAvailable(msg: Message<Unit, Set<Pair<String, String>>>) {
        msg.reply(availableMaps.toSet())
    }

    private fun handleSwitch(msg: Message<Int, Unit>) {
        openedMaps.find { it.id == msg.body }?.let {
            if (selectedMap !== it) {
                selectedMap = it
                sendEvent(Event.GLOBAL_SWITCH_MAP, it)
            }
        }
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        selectedMap = null
        openedMaps.clear()
        availableMaps.clear()
    }

    private fun handleSwitchEnvironment(msg: Message<Dme, Unit>) {
        File(msg.body.rootPath).walkTopDown().forEach {
            if (it.extension == "dmm") {
                availableMaps.add(it.absolutePath to File(msg.body.rootPath).toPath().relativize(it.toPath()).toString())
            }
        }
    }
}
