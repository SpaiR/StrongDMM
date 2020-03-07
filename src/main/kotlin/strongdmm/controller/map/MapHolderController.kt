package strongdmm.controller.map

import gnu.trove.map.hash.TIntObjectHashMap
import io.github.spair.dmm.io.reader.DmmReader
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.save.SaveMap
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.MapId
import java.io.File
import java.nio.file.Files
import kotlin.concurrent.thread

class MapHolderController : EventSender, EventConsumer {
    private val mapsBackupPathsById: TIntObjectHashMap<String> = TIntObjectHashMap()
    private val openedMaps: MutableSet<Dmm> = mutableSetOf()
    private val availableMapsPathsWithVisibleMapsPaths: MutableSet<Pair<String, String>> = mutableSetOf()

    private var selectedMap: Dmm? = null

    init {
        consumeEvent(Event.MapHolderController.Open::class.java, ::handleOpen)
        consumeEvent(Event.MapHolderController.Close::class.java, ::handleClose)
        consumeEvent(Event.MapHolderController.FetchSelected::class.java, ::handleFetchSelected)
        consumeEvent(Event.MapHolderController.Switch::class.java, ::handleSwitch)
        consumeEvent(Event.MapHolderController.Save::class.java, ::handleSave)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.SwitchEnvironment::class.java, ::handleSwitchEnvironment)
    }

    fun postInit() {
        sendEvent(Event.Global.Provider.OpenedMaps(openedMaps))
        sendEvent(Event.Global.Provider.AvailableMaps(availableMapsPathsWithVisibleMapsPaths))
    }

    private fun handleOpen(event: Event<File, Unit>) {
        val id = event.body.absolutePath.hashCode()

        if (selectedMap?.id == id) {
            return
        }

        val dmm = openedMaps.find { it.id == id }

        if (dmm != null) {
            selectedMap = dmm
            sendEvent(Event.Global.SwitchMap(dmm))
        } else {
            val mapFile = event.body

            if (!mapFile.isFile) {
                return
            }

            sendEvent(Event.EnvironmentController.Fetch { environment ->
                val dmmData = DmmReader.readMap(mapFile)
                val map = Dmm(mapFile, dmmData, environment)

                val tmpDmmDataFile = Files.createTempFile("sdmm-", ".dmm.backup").toFile()
                tmpDmmDataFile.writeBytes(mapFile.readBytes())
                mapsBackupPathsById.put(id, tmpDmmDataFile.absolutePath)
                tmpDmmDataFile.deleteOnExit()

                openedMaps.add(map)
                selectedMap = map

                sendEvent(Event.Global.SwitchMap(map))
            })
        }
    }

    private fun handleClose(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            val mapIndex = openedMaps.indexOf(it)

            mapsBackupPathsById.remove(it.id)
            openedMaps.remove(it)
            sendEvent(Event.Global.CloseMap(it))

            if (selectedMap === it) {
                if (openedMaps.isEmpty()) {
                    selectedMap = null
                } else {
                    val index = if (mapIndex == openedMaps.size) mapIndex - 1 else mapIndex
                    val nextMap = openedMaps.toList()[index]
                    selectedMap = nextMap
                    sendEvent(Event.Global.SwitchMap(nextMap))
                }
            }
        }
    }

    private fun handleFetchSelected(event: Event<Unit, Dmm?>) {
        event.reply(selectedMap)
    }

    private fun handleSwitch(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            if (selectedMap !== it) {
                selectedMap = it
                sendEvent(Event.Global.SwitchMap(it))
            }
        }
    }

    private fun handleSave() {
        selectedMap?.let { map ->
            thread(start = true) {
                val initialDmmData = DmmReader.readMap(File(mapsBackupPathsById.get(map.id)))
                SaveMap(map, initialDmmData, true)
            }
        }
    }

    private fun handleResetEnvironment() {
        selectedMap = null
        openedMaps.clear()
        availableMapsPathsWithVisibleMapsPaths.clear()
    }

    private fun handleSwitchEnvironment(event: Event<Dme, Unit>) {
        File(event.body.rootPath).walkTopDown().forEach {
            if (it.extension == "dmm") {
                val absoluteFilePath = it.absolutePath
                val visibleName = File(event.body.rootPath).toPath().relativize(it.toPath()).toString()
                availableMapsPathsWithVisibleMapsPaths.add(absoluteFilePath to visibleName)
            }
        }
    }
}
