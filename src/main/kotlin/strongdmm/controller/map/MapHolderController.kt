package strongdmm.controller.map

import gnu.trove.map.hash.TIntObjectHashMap
import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.StrongDMM
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.parser.DmmParser
import strongdmm.byond.dmm.save.SaveMap
import strongdmm.controller.preferences.Preferences
import strongdmm.event.*
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerActionController
import strongdmm.event.type.controller.TriggerEnvironmentController
import strongdmm.event.type.controller.TriggerMapHolderController
import strongdmm.event.type.ui.TriggerCloseMapDialogUi
import strongdmm.event.type.ui.TriggerSetMapSizeDialogUi
import strongdmm.event.type.ui.TriggerUnknownTypesPanelUi
import strongdmm.ui.closemap.CloseMapDialogStatus
import java.io.File
import java.nio.file.Path

class MapHolderController : EventSender, EventConsumer {
    companion object {
        private val backupsDir: Path = StrongDMM.homeDir.resolve("backups")
    }

    private lateinit var providedPreferences: Preferences
    private lateinit var providedActionBalanceStorage: TObjectIntHashMap<Dmm>

    private val mapsBackupPathsById: TIntObjectHashMap<String> = TIntObjectHashMap()
    private val openedMaps: MutableSet<Dmm> = mutableSetOf()
    private val availableMapsPathsWithVisibleMapsPaths: MutableSet<Pair<String, String>> = mutableSetOf()

    private var selectedMap: Dmm? = null

    init {
        consumeEvent(TriggerMapHolderController.CreateNewMap::class.java, ::handleCreateNewMap)
        consumeEvent(TriggerMapHolderController.OpenMap::class.java, ::handleOpenMap)
        consumeEvent(TriggerMapHolderController.CloseMap::class.java, ::handleCloseMap)
        consumeEvent(TriggerMapHolderController.CloseSelectedMap::class.java, ::handleCloseSelectedMap)
        consumeEvent(TriggerMapHolderController.CloseAllMaps::class.java, ::handleCloseAllMaps)
        consumeEvent(TriggerMapHolderController.FetchSelectedMap::class.java, ::handleFetchSelectedMap)
        consumeEvent(TriggerMapHolderController.ChangeSelectedMap::class.java, ::handleChangeSelectedMap)
        consumeEvent(TriggerMapHolderController.SaveSelectedMap::class.java, ::handleSaveSelectedMap)
        consumeEvent(TriggerMapHolderController.SaveSelectedMapToFile::class.java, ::handleSaveSelectedMapToFile)
        consumeEvent(TriggerMapHolderController.SaveAllMaps::class.java, ::handleSaveAllMaps)
        consumeEvent(TriggerMapHolderController.ChangeActiveZ::class.java, ::handleChangeActiveZ)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Provider.PreferencesControllerPreferences::class.java, ::handleProviderPreferencesControllerPreferences)
        consumeEvent(Provider.ActionControllerActionBalanceStorage::class.java, ::handleProviderActionControllerActionBalanceStorage)
    }

    fun postInit() {
        ensureBackupsDirExists()

        sendEvent(Provider.MapHolderControllerOpenedMaps(openedMaps))
        sendEvent(Provider.MapHolderControllerAvailableMaps(availableMapsPathsWithVisibleMapsPaths))
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

    private fun isMapHasChanges(map: Dmm): Boolean {
        return providedActionBalanceStorage.containsKey(map) && providedActionBalanceStorage[map] != 0
    }

    private fun saveMap(map: Dmm, fileToSave: File? = null) {
        val initialDmmData = DmmParser.parse(File(mapsBackupPathsById.get(map.id)))
        SaveMap(map, initialDmmData, fileToSave, providedPreferences)
        sendEvent(TriggerActionController.ResetActionBalance(map))
    }

    private fun closeMap(map: Dmm) {
        val mapIndex = openedMaps.indexOf(map)

        mapsBackupPathsById.remove(map.id)
        openedMaps.remove(map)
        sendEvent(Reaction.OpenedMapClosed(map))

        if (selectedMap === map) {
            sendEvent(Reaction.SelectedMapClosed())

            if (openedMaps.isEmpty()) {
                selectedMap = null
            } else {
                val index = if (mapIndex == openedMaps.size) mapIndex - 1 else mapIndex
                val nextMap = openedMaps.toList()[index]
                selectedMap = nextMap
                sendEvent(Reaction.SelectedMapChanged(nextMap))
            }
        }
    }

    private fun tryCloseMap(map: Dmm, callback: ((Boolean) -> Unit)? = null) {
        if (isMapHasChanges(map)) {
            sendEvent(TriggerCloseMapDialogUi.Open(map) { closeMapStatus ->
                when (closeMapStatus) {
                    CloseMapDialogStatus.CLOSE_WITH_SAVE -> {
                        saveMap(map)
                        closeMap(map)
                    }
                    CloseMapDialogStatus.CLOSE -> closeMap(map)
                    CloseMapDialogStatus.CANCEL -> {
                    }
                }

                callback?.invoke(closeMapStatus != CloseMapDialogStatus.CANCEL)
            })
        } else {
            closeMap(map)
            callback?.invoke(true)
        }
    }

    private fun handleCreateNewMap(event: Event<File, Unit>) {
        this::class.java.classLoader.getResourceAsStream("new_map_data.txt").use {
            event.body.writeBytes(it!!.readAllBytes())
        }

        sendEvent(TriggerMapHolderController.OpenMap(event.body))
        selectedMap?.setMapSize(0, 0, 0) // -_-
        sendEvent(TriggerSetMapSizeDialogUi.Open())
    }

    private fun handleOpenMap(event: Event<File, Unit>) {
        val id = event.body.absolutePath.hashCode()

        if (selectedMap?.id == id) {
            return
        }

        val dmm = openedMaps.find { it.id == id }

        if (dmm != null) {
            selectedMap = dmm
            sendEvent(Reaction.SelectedMapChanged(dmm))
        } else {
            val mapFile = event.body

            if (!mapFile.isFile) {
                return
            }

            sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment { environment ->
                val dmmData = DmmParser.parse(mapFile)
                val map = Dmm(mapFile, dmmData, environment)

                createBackupFile(environment, mapFile, id)

                openedMaps.add(map)
                selectedMap = map

                sendEvent(Reaction.SelectedMapChanged(map))

                if (map.unknownTypes.isNotEmpty()) {
                    sendEvent(TriggerUnknownTypesPanelUi.Open(map.unknownTypes))
                }
            })
        }
    }

    private fun handleCloseMap(event: Event<MapId, Unit>) {
        openedMaps.find { it.id == event.body }?.let { tryCloseMap(it) }
    }

    private fun handleCloseSelectedMap() {
        selectedMap?.let { map -> tryCloseMap(map) }
    }

    private fun handleCloseAllMaps(event: Event<Unit, MapsCloseStatus>) {
        if (openedMaps.isEmpty()) {
            event.reply(true)
            return
        }

        openedMaps.firstOrNull()?.let { map ->
            tryCloseMap(map) {
                if (it) {
                    handleCloseAllMaps(event)
                } else {
                    event.reply(false)
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
                sendEvent(Reaction.SelectedMapChanged(it))
            }
        }
    }

    private fun handleSaveSelectedMap() {
        selectedMap?.let { saveMap(it) }
    }

    private fun handleSaveSelectedMapToFile(event: Event<File, Unit>) {
        selectedMap?.let { saveMap(it, event.body) }
    }

    private fun handleSaveAllMaps() {
        openedMaps.forEach { saveMap(it) }
    }

    private fun handleChangeActiveZ(event: Event<ActiveZ, Unit>) {
        selectedMap?.let { map ->
            if (event.body == map.zActive || event.body < 1 || event.body > map.maxZ) {
                return
            }

            map.zActive = event.body
            sendEvent(Reaction.SelectedMapZActiveChanged(map.zActive))
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

    private fun handleProviderPreferencesControllerPreferences(event: Event<Preferences, Unit>) {
        providedPreferences = event.body
    }

    private fun handleProviderActionControllerActionBalanceStorage(event: Event<TObjectIntHashMap<Dmm>, Unit>) {
        providedActionBalanceStorage = event.body
    }
}
