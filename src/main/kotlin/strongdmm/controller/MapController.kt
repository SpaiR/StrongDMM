package strongdmm.controller

import io.github.spair.dmm.io.reader.DmmReader
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapId
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.inline.AbsPath
import strongdmm.util.inline.RelPath
import java.io.File

class MapController : EventSender, EventConsumer {
    private val openedMaps: MutableSet<Dmm> = mutableSetOf()
    private val availableMaps: MutableSet<Pair<AbsPath, RelPath>> = mutableSetOf()

    private var selectedMap: Dmm? = null

    init {
        consumeEvent(Event.MapController.Open::class.java, ::handleOpen)
        consumeEvent(Event.MapController.Close::class.java, ::handleClose)
        consumeEvent(Event.MapController.FetchSelected::class.java, ::handleFetchSelected)
        consumeEvent(Event.MapController.FetchAllOpened::class.java, ::handleFetchAllOpened)
        consumeEvent(Event.MapController.FetchAllAvailable::class.java, ::handleFetchAllAvailable)
        consumeEvent(Event.MapController.Switch::class.java, ::handleSwitch)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.SwitchEnvironment::class.java, ::handleSwitchEnvironment)
    }

    private fun handleOpen(event: Event<AbsPath, Unit>) {
        val id = MapId(event.body)

        if (selectedMap?.id == id) {
            return
        }

        val dmm = openedMaps.find { it.id == id }

        if (dmm != null) {
            selectedMap = dmm
            sendEvent(Event.Global.SwitchMap(dmm))
        } else {
            val mapFile = File(event.body.value)

            if (!mapFile.isFile) {
                return
            }

            sendEvent(Event.EnvironmentController.Fetch { environment ->
                val dmmData = DmmReader.readMap(mapFile)
                val map = Dmm(mapFile, dmmData, environment)
                openedMaps.add(map)
                selectedMap = map
                sendEvent(Event.Global.SwitchMap(map))
            })
        }
    }

    private fun handleClose(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            val mapIndex = openedMaps.indexOf(it)

            openedMaps.remove(it)
            sendEvent(Event.Global.CloseMap(it))

            if (selectedMap === it) {
                if (openedMaps.isEmpty()) {
                    selectedMap = null
                } else {
                    val index = if (mapIndex == openedMaps.size) mapIndex - 1 else mapIndex
                    selectedMap = openedMaps.toList()[index]
                    sendEvent(Event.Global.SwitchMap(selectedMap!!))
                }
            }
        }
    }

    private fun handleFetchSelected(event: Event<Unit, Dmm?>) {
        event.reply(selectedMap)
    }

    private fun handleFetchAllOpened(event: Event<Unit, Set<Dmm>>) {
        event.reply(openedMaps.toSet())
    }

    private fun handleFetchAllAvailable(event: Event<Unit, Set<Pair<AbsPath, RelPath>>>) {
        event.reply(availableMaps.toSet())
    }

    private fun handleSwitch(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            if (selectedMap !== it) {
                selectedMap = it
                sendEvent(Event.Global.SwitchMap(it))
            }
        }
    }

    private fun handleResetEnvironment() {
        selectedMap = null
        openedMaps.clear()
        availableMaps.clear()
    }

    private fun handleSwitchEnvironment(event: Event<Dme, Unit>) {
        File(event.body.rootPath).walkTopDown().forEach {
            if (it.extension == "dmm") {
                val abs = AbsPath(it.absolutePath)
                val rel =
                    RelPath(File(event.body.rootPath).toPath().relativize(it.toPath()).toString())
                availableMaps.add(abs to rel)
            }
        }
    }
}
