package strongdmm.controller

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import strongdmm.StrongDMM
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventEnvironmentController
import strongdmm.event.type.controller.EventRecentFilesController
import java.io.File

class RecentFilesController : EventConsumer, EventSender {
    companion object {
        private const val RECENT_ENVIRONMENTS_KEY = "recent_environments"
        private const val READABLE_MAP_PATH_KEY = "readable"
        private const val ABSOLUTE_MAP_PATH_KEY = "absolute"

        private val recentJsonFile: File = File(StrongDMM.homeDir.toFile(), "recent.json")
    }

    private val recentEnvironments: MutableList<String> = mutableListOf()
    private val recentMaps: MutableList<MapPath> = mutableListOf() // list will be updated after current environment is changed

    private val allRecentMaps: MutableMap<String, MutableList<MapPath>> = mutableMapOf()

    init {
        consumeEvent(EventGlobal.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(EventRecentFilesController.ClearRecentEnvironments::class.java, ::handleClearRecentEnvironments)
        consumeEvent(EventRecentFilesController.ClearRecentMaps::class.java, ::handleClearRecentMaps)
    }

    fun postInit() {
        ensureRecentJsonFileExists()
        readRecentJsonFile()

        sendEvent(EventGlobalProvider.RecentFilesControllerRecentEnvironments(recentEnvironments))
        sendEvent(EventGlobalProvider.RecentFilesControllerRecentMaps(recentMaps))
    }

    private fun ensureRecentJsonFileExists() {
        if (recentJsonFile.createNewFile()) {
            recentJsonFile.appendText("{\"$RECENT_ENVIRONMENTS_KEY\":{}}")
        }
    }

    private fun readRecentJsonFile() {
        recentJsonFile.reader().use {
            val json = Json.parse(it).asObject()
            val recent = json.get(RECENT_ENVIRONMENTS_KEY).asObject()

            recent.forEach { recentEnv ->
                val envPath = recentEnv.name
                recentEnvironments.add(envPath)
                recentEnv.value.asArray().forEach { recentMap ->
                    recentMap.asObject().let { path ->
                        val mapPath = MapPath(path[READABLE_MAP_PATH_KEY].asString(), path[ABSOLUTE_MAP_PATH_KEY].asString())
                        allRecentMaps.getOrPut(envPath) { mutableListOf() }.add(mapPath)
                    }
                }
            }
        }
    }

    private fun writeRecentJsonFile() {
        recentJsonFile.writeText(JsonObject().apply {
            add(RECENT_ENVIRONMENTS_KEY, JsonObject().apply {
                recentEnvironments.forEach { environmentPath ->
                    add(environmentPath, JsonArray().apply {
                        allRecentMaps[environmentPath]?.forEach { mapPath ->
                            add(JsonObject().apply {
                                add(READABLE_MAP_PATH_KEY, mapPath.readable)
                                add(ABSOLUTE_MAP_PATH_KEY, mapPath.absolute)
                            })
                        }
                    })
                }
            })
        }.toString())
    }

    private fun addEnvironment(environmentPath: String) {
        recentEnvironments.remove(environmentPath)
        recentEnvironments.add(0, environmentPath)
        writeRecentJsonFile()
    }

    private fun addMap(environmentPath: String, mapPath: MapPath) {
        val maps = allRecentMaps.getOrPut(environmentPath) { mutableListOf() }
        maps.remove(mapPath)
        maps.add(0, mapPath)
        writeRecentJsonFile()
    }

    private fun updateRecentMapsList(currentEnvironmentPath: String) {
        recentMaps.clear()
        recentMaps.addAll(allRecentMaps.getOrPut(currentEnvironmentPath) { mutableListOf() })
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        updateRecentMapsList(event.body.absEnvPath)
        addEnvironment(event.body.absEnvPath)
    }

    private fun handleEnvironmentReset() {
        recentMaps.clear()
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        sendEvent(EventEnvironmentController.FetchOpenedEnvironment { environment ->
            addMap(environment.absEnvPath, event.body.mapPath)
            updateRecentMapsList(environment.absEnvPath)
        })
    }

    private fun handleClearRecentEnvironments() {
        sendEvent(EventEnvironmentController.FetchOpenedEnvironment { environment ->
            recentEnvironments.clear()
            writeRecentJsonFile()
            updateRecentMapsList(environment.absEnvPath)
        })
    }

    private fun handleClearRecentMaps() {
        sendEvent(EventEnvironmentController.FetchOpenedEnvironment { environment ->
            allRecentMaps[environment.absEnvPath]?.clear()
            writeRecentJsonFile()
            updateRecentMapsList(environment.absEnvPath)
        })
    }
}
