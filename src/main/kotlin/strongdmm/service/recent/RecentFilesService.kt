package strongdmm.service.recent

import com.fasterxml.jackson.databind.ObjectMapper
import strongdmm.PostInitialize
import strongdmm.Service
import strongdmm.StrongDMM
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerRecentFilesService
import java.io.File

class RecentFilesService : Service, EventHandler, PostInitialize {
    companion object {
        private val recentFilesConfig: File = File(StrongDMM.homeDir.toFile(), "recent.json")
    }

    private lateinit var recentFiles: RecentFiles

    private val objectMapper: ObjectMapper = ObjectMapper()

    private val recentEnvironments: MutableList<String> = mutableListOf()
    private val recentMaps: MutableList<MapPath> = mutableListOf()

    init {
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(TriggerRecentFilesService.ClearRecentEnvironments::class.java, ::handleClearRecentEnvironments)
        consumeEvent(TriggerRecentFilesService.ClearRecentMaps::class.java, ::handleClearRecentMaps)
    }

    override fun postInit() {
        ensureRecentFilesConfigExists()
        readRecentFilesConfig()

        sendEvent(Provider.RecentFilesServiceRecentEnvironmentsWithMaps(recentFiles.maps))
        sendEvent(Provider.RecentFilesServiceRecentEnvironments(recentEnvironments))
        sendEvent(Provider.RecentFilesServiceRecentMaps(recentMaps))
    }

    private fun ensureRecentFilesConfigExists() {
        if (recentFilesConfig.createNewFile()) {
            writeRecentJsonFile(RecentFiles())
        }
    }

    private fun readRecentFilesConfig() {
        try {
            recentFiles = objectMapper.readValue(recentFilesConfig, RecentFiles::class.java)
            validateRecentFiles()
            updateRecentEnvironmentsList()
        } catch (e: Exception) {
            writeRecentJsonFile(RecentFiles())
            readRecentFilesConfig()
        }
    }

    private fun writeRecentJsonFile(recentFiles: RecentFiles) {
        objectMapper.writeValue(recentFilesConfig, recentFiles)
    }

    private fun validateRecentFiles() {
        recentFiles.environments.toList().forEach {
            if (!File(it).exists()) {
                recentFiles.environments.remove(it)
            }
        }
        recentFiles.maps.toMap().forEach { (envPath, mapPaths) ->
            if (File(envPath).exists()) {
                mapPaths.toList().forEach { mapPath ->
                    if (!File(mapPath.absolute).exists()) {
                        mapPaths.remove(mapPath)
                    }
                }
            } else {
                recentFiles.maps.remove(envPath)
            }
        }
    }

    private fun addEnvironment(environmentPath: String) {
        recentFiles.environments.remove(environmentPath)
        recentFiles.environments.add(0, environmentPath)
        writeRecentJsonFile(recentFiles)
        updateRecentEnvironmentsList()
    }

    private fun addMap(environmentPath: String, mapPath: MapPath) {
        val maps = recentFiles.maps.getOrPut(environmentPath) { mutableListOf() }
        maps.remove(mapPath)
        maps.add(0, mapPath)
        writeRecentJsonFile(recentFiles)
        updateRecentMapsList(environmentPath)
    }

    private fun updateRecentEnvironmentsList() {
        recentEnvironments.clear()
        recentEnvironments.addAll(recentFiles.environments)
    }

    private fun updateRecentMapsList(currentEnvironmentPath: String) {
        recentMaps.clear()
        recentMaps.addAll(recentFiles.maps.getOrPut(currentEnvironmentPath) { mutableListOf() })
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        addEnvironment(event.body.absEnvPath)
        updateRecentMapsList(event.body.absEnvPath)
    }

    private fun handleEnvironmentReset() {
        recentMaps.clear()
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment { environment ->
            addMap(environment.absEnvPath, event.body.mapPath)
        })
    }

    private fun handleClearRecentEnvironments() {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment { environment ->
            recentFiles.environments.clear()
            writeRecentJsonFile(recentFiles)
            updateRecentMapsList(environment.absEnvPath)
        })
    }

    private fun handleClearRecentMaps() {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment { environment ->
            recentFiles.maps[environment.absEnvPath]?.clear()
            writeRecentJsonFile(recentFiles)
            updateRecentMapsList(environment.absEnvPath)
        })
    }
}
