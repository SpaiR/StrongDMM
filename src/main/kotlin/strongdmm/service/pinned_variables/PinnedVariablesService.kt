package strongdmm.service.pinned_variables

import com.google.gson.Gson
import strongdmm.PostInitialize
import strongdmm.Service
import strongdmm.StrongDMM
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerPinnedVariablesService
import java.io.File

class PinnedVariablesService : Service, EventHandler, PostInitialize {
    companion object {
        private val pinnedVariablesConfig: File = File(StrongDMM.homeDir.toFile(), "pinned_variables.json")
    }

    init {
        consumeEvent(TriggerPinnedVariablesService.FetchPinnedVariables::class.java, ::handleFetchPinnedVariables)
        consumeEvent(TriggerPinnedVariablesService.PinVariable::class.java, ::handlePinVariables)
        consumeEvent(TriggerPinnedVariablesService.UnpinVariable::class.java, ::handleUnpinVariable)
    }

    private lateinit var pinnedVariables: PinnedVariables

    override fun postInit() {
        ensurePinnedVariablesConfigExists()
        readPinnedVariablesConfig()
    }

    private fun ensurePinnedVariablesConfigExists() {
        if (pinnedVariablesConfig.createNewFile()) {
            pinnedVariablesConfig.writeText(Gson().toJson(PinnedVariables()))
        }
    }

    private fun readPinnedVariablesConfig() {
        pinnedVariablesConfig.reader().use {
            pinnedVariables = Gson().fromJson(it, PinnedVariables::class.java)
        }
    }

    private fun writePinnedVariablesConfig() {
        pinnedVariablesConfig.writeText(Gson().toJson(pinnedVariables))
    }

    private fun handleFetchPinnedVariables(event: Event<Unit, Set<String>>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            event.reply(pinnedVariables.pinnedVariables.getOrPut(it.absEnvPath) { mutableSetOf() })
        })
    }

    private fun handlePinVariables(event: Event<String, Unit>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            pinnedVariables.pinnedVariables.getOrPut(it.absEnvPath) { mutableSetOf() }.add(event.body)
            writePinnedVariablesConfig()
        })
    }

    private fun handleUnpinVariable(event: Event<String, Unit>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            pinnedVariables.pinnedVariables[it.absEnvPath]?.remove(event.body)
            writePinnedVariablesConfig()
        })
    }
}
