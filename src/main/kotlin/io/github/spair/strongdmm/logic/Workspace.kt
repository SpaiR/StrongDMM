package io.github.spair.strongdmm.logic

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import java.io.File
import java.util.SortedSet

object Workspace {

    private const val WORKSPACE_FILE_NAME = ".workspace.json"
    private val holder: WorkspaceHolder = WorkspaceHolder()

    init {
        load()
    }

    fun isTgmSaveMode(): Boolean = holder.isTgmSaveMode

    fun setTgmSaveMode(value: Boolean) {
        holder.isTgmSaveMode = value
        save()
    }

    fun getRecentEnvironmentsPaths(): List<String> = holder.recentEnvironments.map { it.path }.toList()
    fun getRecentMapsPaths(envPath: String): Set<String> = findEnvironment(envPath)?.recentMaps ?: emptySet()

    fun addRecentEnvironment(envPath: String) {
        if (findEnvironment(envPath) == null) {
            holder.recentEnvironments.add(Environment(envPath))
            save()
        }
    }

    fun removeRecentEnvironment(envPath: String) {
        findEnvironment(envPath)?.let {
            holder.recentEnvironments.remove(it)
            save()
        }
    }

    fun addRecentMap(envPath: String, mapPath: String) {
        val env = findEnvironment(envPath) ?: return
        env.recentMaps.add(mapPath)
        save()
    }

    fun removeRecentMap(envPath: String, mapPath: String) {
        val env = findEnvironment(envPath) ?: return
        env.recentMaps.remove(mapPath)
        save()
    }

    private fun load() {
        val workspace = File(WORKSPACE_FILE_NAME)

        if (workspace.exists()) {
            workspace.reader().use { reader ->
                val json = Json.parse(reader).asObject()
                val recentEnvironments = json.get(WorkspaceHolder::recentEnvironments.name).asArray()

                recentEnvironments.forEach { entry ->
                    val envJson = entry.asObject()
                    val path = envJson.get(Environment::path.name).asString()
                    val recentMaps = sortedSetOf<String>()

                    envJson.get(Environment::recentMaps.name).asArray().forEach {
                        recentMaps.add(it.asString())
                    }

                    holder.recentEnvironments.add(Environment(path, recentMaps))
                }

                holder.isTgmSaveMode = json.getBoolean(WorkspaceHolder::isTgmSaveMode.name, true)
            }
        }
    }

    private fun save() {
        val workspace = File(WORKSPACE_FILE_NAME)
        workspace.createNewFile()

        val json = JsonObject()
        val recentEnvironments = JsonArray()

        holder.recentEnvironments.forEach { env ->
            val envJson = JsonObject()
            envJson.add(Environment::path.name, env.path)

            val recentMapsJson = JsonArray()
            env.recentMaps.forEach { recentMapsJson.add(it) }
            envJson.add(Environment::recentMaps.name, recentMapsJson)

            recentEnvironments.add(envJson)
        }

        json.add(WorkspaceHolder::isTgmSaveMode.name, holder.isTgmSaveMode)
        json.add(WorkspaceHolder::recentEnvironments.name, recentEnvironments)
        workspace.writeText(json.toString())
    }

    private fun findEnvironment(envPath: String): Environment? = holder.recentEnvironments.find { it.path == envPath }

    private class WorkspaceHolder(
        var isTgmSaveMode: Boolean = true,
        val recentEnvironments: MutableList<Environment> = mutableListOf()
    )

    private class Environment(
        val path: String,
        val recentMaps: SortedSet<String> = sortedSetOf()
    )
}
