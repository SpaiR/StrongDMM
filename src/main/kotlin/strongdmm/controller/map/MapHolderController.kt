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
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventEnvironmentController
import strongdmm.event.type.controller.EventMapHolderController
import java.io.File
import java.nio.file.Files
import kotlin.concurrent.thread

class MapHolderController : EventSender, EventConsumer {
    private val mapsBackupPathsById: TIntObjectHashMap<String> = TIntObjectHashMap()
    private val openedMaps: MutableSet<Dmm> = mutableSetOf()
    private val availableMapsPathsWithVisibleMapsPaths: MutableSet<Pair<String, String>> = mutableSetOf()

    private var openedMap: Dmm? = null

    init {
        consumeEvent(EventMapHolderController.Open::class.java, ::handleOpen)
        consumeEvent(EventMapHolderController.Close::class.java, ::handleClose)
        consumeEvent(EventMapHolderController.FetchSelected::class.java, ::handleFetchSelected)
        consumeEvent(EventMapHolderController.Switch::class.java, ::handleSwitch)
        consumeEvent(EventMapHolderController.Save::class.java, ::handleSave)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
    }

    fun postInit() {
        sendEvent(EventGlobalProvider.OpenedMaps(openedMaps))
        sendEvent(EventGlobalProvider.AvailableMaps(availableMapsPathsWithVisibleMapsPaths))
    }

    private fun handleOpen(event: Event<File, Unit>) {
        val id = event.body.absolutePath.hashCode()

        if (openedMap?.id == id) {
            return
        }

        val dmm = openedMaps.find { it.id == id }

        if (dmm != null) {
            openedMap = dmm
            sendEvent(EventGlobal.OpenedMapChanged(dmm))
        } else {
            val mapFile = event.body

            if (!mapFile.isFile) {
                return
            }

            sendEvent(EventEnvironmentController.Fetch { environment ->
                val dmmData = DmmReader.readMap(mapFile)
                val map = Dmm(mapFile, dmmData, environment)

                val tmpDmmDataFile = Files.createTempFile("sdmm-", ".dmm.backup").toFile()
                tmpDmmDataFile.writeBytes(mapFile.readBytes())
                mapsBackupPathsById.put(id, tmpDmmDataFile.absolutePath)
                tmpDmmDataFile.deleteOnExit()

                openedMaps.add(map)
                openedMap = map

                sendEvent(EventGlobal.OpenedMapChanged(map))
            })
        }
    }

    private fun handleClose(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            val mapIndex = openedMaps.indexOf(it)

            mapsBackupPathsById.remove(it.id)
            openedMaps.remove(it)
            sendEvent(EventGlobal.OpenedMapClosed(it))

            if (openedMap === it) {
                if (openedMaps.isEmpty()) {
                    openedMap = null
                } else {
                    val index = if (mapIndex == openedMaps.size) mapIndex - 1 else mapIndex
                    val nextMap = openedMaps.toList()[index]
                    openedMap = nextMap
                    sendEvent(EventGlobal.OpenedMapChanged(nextMap))
                }
            }
        }
    }

    private fun handleFetchSelected(event: Event<Unit, Dmm>) {
        openedMap?.let { event.reply(it) }
    }

    private fun handleSwitch(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            if (openedMap !== it) {
                openedMap = it
                sendEvent(EventGlobal.OpenedMapChanged(it))
            }
        }
    }

    private fun handleSave() {
        openedMap?.let { map ->
            thread(start = true) {
                val initialDmmData = DmmReader.readMap(File(mapsBackupPathsById.get(map.id)))
                SaveMap(map, initialDmmData, true)
            }
        }
    }

    private fun handleEnvironmentReset() {
        openedMap = null
        openedMaps.clear()
        availableMapsPathsWithVisibleMapsPaths.clear()
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        File(event.body.rootPath).walkTopDown().forEach {
            if (it.extension == "dmm") {
                val absoluteFilePath = it.absolutePath
                val visibleName = File(event.body.rootPath).toPath().relativize(it.toPath()).toString()
                availableMapsPathsWithVisibleMapsPaths.add(absoluteFilePath to visibleName)
            }
        }
    }
}
