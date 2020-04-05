package strongdmm.controller.map

import gnu.trove.map.hash.TIntObjectHashMap
import strongdmm.StrongDMM
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.parser.DmmParser
import strongdmm.byond.dmm.save.SaveMap
import strongdmm.event.*
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventEnvironmentController
import strongdmm.event.type.controller.EventMapHolderController
import java.io.File
import java.nio.file.Path
import kotlin.concurrent.thread

class MapHolderController : EventSender, EventConsumer {
    companion object {
        private val backupsDir: Path = StrongDMM.homeDir.resolve("backups")
    }

    private val mapsBackupPathsById: TIntObjectHashMap<String> = TIntObjectHashMap()
    private val openedMaps: MutableSet<Dmm> = mutableSetOf()
    private val availableMapsPathsWithVisibleMapsPaths: MutableSet<Pair<String, String>> = mutableSetOf()

    private var selectedMap: Dmm? = null

    init {
        consumeEvent(EventMapHolderController.OpenMap::class.java, ::handleOpenMap)
        consumeEvent(EventMapHolderController.CloseMap::class.java, ::handleCloseMap)
        consumeEvent(EventMapHolderController.FetchSelectedMap::class.java, ::handleFetchSelectedMap)
        consumeEvent(EventMapHolderController.ChangeSelectedMap::class.java, ::handleChangeSelectedMap)
        consumeEvent(EventMapHolderController.SaveSelectedMap::class.java, ::handleSaveSelectedMap)
        consumeEvent(EventMapHolderController.ChangeActiveZ::class.java, ::handleChangeActiveZ)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
    }

    fun postInit() {
        ensureBackupsDirExists()

        sendEvent(EventGlobalProvider.MapHolderControllerOpenedMaps(openedMaps))
        sendEvent(EventGlobalProvider.MapHolderControllerAvailableMaps(availableMapsPathsWithVisibleMapsPaths))
    }

    private fun ensureBackupsDirExists() {
        backupsDir.toFile().mkdirs()
    }

    private fun createBackupFile(environment: Dme, mapFile: File, id: Int) {
        val tmpFileName = "${environment.name}-${mapFile.nameWithoutExtension}-${System.currentTimeMillis()}.backup"
        val tmpDmmDataFile = File(backupsDir.toFile(), tmpFileName)

        tmpDmmDataFile.createNewFile()
        tmpDmmDataFile.writeBytes(mapFile.readBytes())
        tmpDmmDataFile.deleteOnExit()

        mapsBackupPathsById.put(id, tmpDmmDataFile.absolutePath)
    }

    private fun handleOpenMap(event: Event<File, Unit>) {
        val id = event.body.absolutePath.hashCode()

        if (selectedMap?.id == id) {
            return
        }

        val dmm = openedMaps.find { it.id == id }

        if (dmm != null) {
            selectedMap = dmm
            sendEvent(EventGlobal.SelectedMapChanged(dmm))
        } else {
            val mapFile = event.body

            if (!mapFile.isFile) {
                return
            }

            sendEvent(EventEnvironmentController.FetchOpenedEnvironment { environment ->
                val dmmData = DmmParser.parse(mapFile)
                val map = Dmm(mapFile, dmmData, environment)

                createBackupFile(environment, mapFile, id)

                openedMaps.add(map)
                selectedMap = map

                sendEvent(EventGlobal.SelectedMapChanged(map))
            })
        }
    }

    private fun handleCloseMap(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            val mapIndex = openedMaps.indexOf(it)

            mapsBackupPathsById.remove(it.id)
            openedMaps.remove(it)
            sendEvent(EventGlobal.OpenedMapClosed(it))

            if (selectedMap === it) {
                if (openedMaps.isEmpty()) {
                    selectedMap = null
                } else {
                    val index = if (mapIndex == openedMaps.size) mapIndex - 1 else mapIndex
                    val nextMap = openedMaps.toList()[index]
                    selectedMap = nextMap
                    sendEvent(EventGlobal.SelectedMapChanged(nextMap))
                }
            }
        }
    }

    private fun handleFetchSelectedMap(event: Event<Unit, Dmm>) {
        selectedMap?.let { event.reply(it) }
    }

    private fun handleChangeSelectedMap(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            if (selectedMap !== it) {
                selectedMap = it
                sendEvent(EventGlobal.SelectedMapChanged(it))
            }
        }
    }

    private fun handleSaveSelectedMap() {
        selectedMap?.let { map ->
            thread(start = true) {
                val initialDmmData = DmmParser.parse(File(mapsBackupPathsById.get(map.id)))
                SaveMap(map, initialDmmData, true)
            }
        }
    }

    private fun handleChangeActiveZ(event: Event<ActiveZ, Unit>) {
        selectedMap?.let { map ->
            if (event.body == map.zActive || event.body < 1 || event.body > map.maxZ) {
                return
            }

            map.zActive = event.body
            sendEvent(EventGlobal.SelectedMapZActiveChanged(map.zActive))
        }
    }

    private fun handleEnvironmentReset() {
        selectedMap = null
        openedMaps.clear()
        availableMapsPathsWithVisibleMapsPaths.clear()
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        File(event.body.absRootDirPath).walkTopDown().forEach {
            if (it.extension == "dmm") {
                val absoluteFilePath = it.absolutePath
                val visibleName = File(event.body.absRootDirPath).toPath().relativize(it.toPath()).toString()
                availableMapsPathsWithVisibleMapsPaths.add(absoluteFilePath to visibleName)
            }
        }
    }
}
