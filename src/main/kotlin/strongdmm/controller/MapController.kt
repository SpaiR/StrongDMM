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
    private val openedMaps: MutableList<Dmm> = mutableListOf()
    private var selectedMap: Dmm? = null

    init {
        consumeEvent(Event.MAP_OPEN, ::handleOpen)
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
    }

    private fun handleOpen(msg: Message<String, Unit>) {
        if (selectedMap?.mapPath == msg.body) {
            return
        }

        val dmm = openedMaps.find { it.mapPath == msg.body }

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
                selectedMap = Dmm(mapFile, dmmData, environment).apply { openedMaps.add(this) }
                sendEvent(Event.GLOBAL_SWITCH_MAP, selectedMap)
            }
        }
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        selectedMap = null
        openedMaps.clear()
    }
}
