package strongdmm.service.pinned_variables

import strongdmm.Service
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerPinnedVariablesService
import strongdmm.event.type.service.TriggerSettingsService
import strongdmm.service.settings.Settings

class PinnedVariablesService : Service, EventHandler {
    init {
        consumeEvent(Provider.SettingsServiceSettings::class.java, ::handleProviderSettingsServiceSettings)
        consumeEvent(TriggerPinnedVariablesService.FetchPinnedVariables::class.java, ::handleFetchPinnedVariables)
        consumeEvent(TriggerPinnedVariablesService.PinVariable::class.java, ::handlePinVariables)
        consumeEvent(TriggerPinnedVariablesService.UnpinVariable::class.java, ::handleUnpinVariable)
    }

    private lateinit var pinnedVariablesServiceSettings: PinnedVariablesServiceSettings

    private fun handleProviderSettingsServiceSettings(event: Event<Settings, Unit>) {
        pinnedVariablesServiceSettings = event.body.pinnedVariablesServiceSettings
    }

    private fun handleFetchPinnedVariables(event: Event<Unit, Set<String>>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            event.reply(pinnedVariablesServiceSettings.pinnedVariables.getOrPut(it.absEnvPath) { mutableSetOf() })
        })
    }

    private fun handlePinVariables(event: Event<String, Unit>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            pinnedVariablesServiceSettings.pinnedVariables.getOrPut(it.absEnvPath) { mutableSetOf() }.add(event.body)
            sendEvent(TriggerSettingsService.SaveSettings())
        })
    }

    private fun handleUnpinVariable(event: Event<String, Unit>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            pinnedVariablesServiceSettings.pinnedVariables[it.absEnvPath]?.remove(event.body)
            sendEvent(TriggerSettingsService.SaveSettings())
        })
    }
}
