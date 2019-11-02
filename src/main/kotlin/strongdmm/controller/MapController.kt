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
        val mapFile = File(msg.body)

        if (!mapFile.isFile || selectedMap?.mapPath == mapFile.absolutePath) {
            return
        }

        sendEvent<Dme>(Event.ENVIRONMENT_FETCH) { environment ->
            val dmm = openedMaps.find { it.mapPath == mapFile.absolutePath }?.let { it } ?: run {
                val dmmData = DmmReader.readMap(mapFile)
                Dmm(mapFile, dmmData, environment).apply { openedMaps.add(this) }
            }

            selectedMap = dmm

            sendEvent(Event.GLOBAL_SWITCH_MAP, dmm)
        }
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        selectedMap = null
        openedMaps.clear()
    }
}
