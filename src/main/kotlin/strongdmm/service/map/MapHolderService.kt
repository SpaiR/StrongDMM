package strongdmm.service.map

import gnu.trove.map.hash.TIntObjectHashMap
import strongdmm.application.PostInitialize
import strongdmm.application.Service
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPath
import strongdmm.byond.dmm.parser.DmmParser
import strongdmm.byond.dmm.save.SaveMap
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.*
import strongdmm.event.type.ui.TriggerConfirmationDialogUi
import strongdmm.event.type.ui.TriggerNotificationPanelUi
import strongdmm.event.type.ui.TriggerSetMapSizeDialogUi
import strongdmm.service.action.ActionBalanceStorage
import strongdmm.service.preferences.Preferences
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogData
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogStatus
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogType
import java.io.File
import java.nio.file.Files

class MapHolderService : Service, PostInitialize {
    private lateinit var providedPreferences: Preferences
    private lateinit var providedActionBalanceStorage: ActionBalanceStorage

    private val mapsBackupPathsById: TIntObjectHashMap<String> = TIntObjectHashMap()
    private val openedMaps: MutableSet<Dmm> = mutableSetOf()
    private val availableMapsPaths: MutableSet<MapPath> = mutableSetOf()

    private var selectedMap: Dmm? = null

    init {
        EventBus.sign(TriggerMapHolderService.CreateNewMap::class.java, ::handleCreateNewMap)
        EventBus.sign(TriggerMapHolderService.OpenMap::class.java, ::handleOpenMap)
        EventBus.sign(TriggerMapHolderService.CloseMap::class.java, ::handleCloseMap)
        EventBus.sign(TriggerMapHolderService.CloseSelectedMap::class.java, ::handleCloseSelectedMap)
        EventBus.sign(TriggerMapHolderService.CloseAllMaps::class.java, ::handleCloseAllMaps)
        EventBus.sign(TriggerMapHolderService.FetchSelectedMap::class.java, ::handleFetchSelectedMap)
        EventBus.sign(TriggerMapHolderService.ChangeSelectedMap::class.java, ::handleChangeSelectedMap)
        EventBus.sign(TriggerMapHolderService.SaveSelectedMap::class.java, ::handleSaveSelectedMap)
        EventBus.sign(TriggerMapHolderService.SaveSelectedMapToFile::class.java, ::handleSaveSelectedMapToFile)
        EventBus.sign(TriggerMapHolderService.SaveAllMaps::class.java, ::handleSaveAllMaps)
        EventBus.sign(TriggerMapHolderService.ChangeSelectedZ::class.java, ::handleChangeSelectedZ)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionEnvironmentService.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(ProviderPreferencesService.Preferences::class.java, ::handleProviderPreferences)
        EventBus.sign(ProviderActionService.ActionBalanceStorage::class.java, ::handleProviderActionBalanceStorage)
    }

    override fun postInit() {
        EventBus.post(ProviderMapHolderService.OpenedMaps(openedMaps))
        EventBus.post(ProviderMapHolderService.AvailableMaps(availableMapsPaths))
    }

    private fun createBackupFile(mapFile: File, id: Int) {
        val tmpDmmDataFile = Files.createTempFile("sdmm.", ".backup").toFile()

        tmpDmmDataFile.writeBytes(mapFile.readBytes())
        tmpDmmDataFile.deleteOnExit()

        mapsBackupPathsById.put(id, tmpDmmDataFile.absolutePath)
    }

    private fun isMapHasChanges(map: Dmm): Boolean {
        return providedActionBalanceStorage.isMapModified(map)
    }

    private fun saveMap(map: Dmm, fileToSave: File? = null) {
        val initialDmmData = DmmParser.parse(File(mapsBackupPathsById.get(map.id)))
        SaveMap(map, initialDmmData, fileToSave, providedPreferences)
        EventBus.post(TriggerActionService.ResetActionBalance(map))
        EventBus.post(TriggerNotificationPanelUi.Notify("${map.mapName} saved!"))
    }

    private fun closeMap(map: Dmm) {
        val mapIndex = openedMaps.indexOf(map)

        mapsBackupPathsById.remove(map.id)
        openedMaps.remove(map)
        EventBus.post(ReactionMapHolderService.OpenedMapClosed(map))

        if (selectedMap === map) {
            EventBus.post(ReactionMapHolderService.SelectedMapClosed.SIGNAL)

            if (openedMaps.isEmpty()) {
                selectedMap = null
            } else {
                val index = if (mapIndex == openedMaps.size) mapIndex - 1 else mapIndex
                val nextMap = openedMaps.toList()[index]
                selectedMap = nextMap
                EventBus.post(ReactionMapHolderService.SelectedMapChanged(nextMap))
            }
        }
    }

    private fun tryCloseMap(map: Dmm, callback: ((Boolean) -> Unit)? = null) {
        if (isMapHasChanges(map)) {
            val confirmTitle = "Save Map?"
            val confirmQuestion = "Map \"${map.mapName}\" has been modified. Save changes?"
            val confirmData = ConfirmationDialogData(ConfirmationDialogType.YES_NO_CANCEL, confirmTitle, confirmQuestion)

            EventBus.post(TriggerConfirmationDialogUi.Open(confirmData) { confirmationStatus ->
                var mapClosed = true

                when (confirmationStatus) {
                    ConfirmationDialogStatus.YES -> {
                        saveMap(map)
                        closeMap(map)
                    }
                    ConfirmationDialogStatus.NO -> {
                        closeMap(map)
                    }
                    ConfirmationDialogStatus.CANCEL -> {
                        mapClosed = false
                    }
                }

                callback?.invoke(mapClosed)
            })
        } else {
            closeMap(map)
            callback?.invoke(true)
        }
    }

    private fun handleCreateNewMap(event: Event<File, Unit>) {
        var newMapFile: File = event.body

        if (!newMapFile.exists() && event.body.extension != "dmm") {
            newMapFile = File(event.body.parent, "${event.body.name}.dmm")
        }

        this::class.java.classLoader.getResourceAsStream("txt/new_map_data.txt").use {
            newMapFile.writeBytes(it!!.readAllBytes())
        }

        EventBus.post(TriggerMapHolderService.OpenMap(newMapFile))
        selectedMap?.setMapSize(0, 0, 0) // -_-
        EventBus.post(TriggerSetMapSizeDialogUi.Open())
    }

    private fun handleOpenMap(event: Event<File, Unit>) {
        val id = event.body.absolutePath.hashCode()

        if (selectedMap?.id == id) {
            return
        }

        val dmm = openedMaps.find { it.id == id }

        if (dmm != null) {
            selectedMap = dmm
            EventBus.post(ReactionMapHolderService.SelectedMapChanged(dmm))
        } else {
            val mapFile = event.body

            if (!mapFile.isFile) {
                return
            }

            EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment { environment ->
                val dmmData = DmmParser.parse(mapFile)

                EventBus.post(TriggerMapPreprocessService.Preprocess(dmmData) {
                    val map = Dmm(mapFile, dmmData, environment)

                    createBackupFile(mapFile, id)

                    openedMaps.add(map)
                    selectedMap = map

                    EventBus.post(ReactionMapHolderService.SelectedMapChanged(map))
                })
            })
        }
    }

    private fun handleCloseMap(event: Event<Int, Unit>) {
        openedMaps.find { it.id == event.body }?.let { tryCloseMap(it) }
    }

    private fun handleCloseSelectedMap() {
        selectedMap?.let { map -> tryCloseMap(map) }
    }

    private fun handleCloseAllMaps(event: Event<Unit, Boolean>) {
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

    private fun handleChangeSelectedMap(event: Event<Int, Unit>) {
        openedMaps.find { it.id == event.body }?.let {
            if (selectedMap !== it) {
                selectedMap = it
                EventBus.post(ReactionMapHolderService.SelectedMapChanged(it))
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

    private fun handleChangeSelectedZ(event: Event<Int, Unit>) {
        selectedMap?.let { map ->
            if (event.body == map.zSelected || event.body < 1 || event.body > map.maxZ) {
                return
            }

            map.zSelected = event.body
            EventBus.post(ReactionMapHolderService.SelectedMapZSelectedChanged(map.zSelected))
        }
    }

    private fun handleEnvironmentReset() {
        selectedMap = null
        openedMaps.clear()
        availableMapsPaths.clear()
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        File(event.body.absRootDirPath).walkTopDown().forEach {
            if (it.extension == "dmm") {
                val absoluteFilePath = it.absolutePath
                val mapFile = File(event.body.absRootDirPath)
                val readableName = mapFile.toPath().relativize(it.toPath()).toString()
                availableMapsPaths.add(MapPath(readableName, absoluteFilePath))
            }
        }
    }

    private fun handleProviderPreferences(event: Event<Preferences, Unit>) {
        providedPreferences = event.body
    }

    private fun handleProviderActionBalanceStorage(event: Event<ActionBalanceStorage, Unit>) {
        providedActionBalanceStorage = event.body
    }
}
