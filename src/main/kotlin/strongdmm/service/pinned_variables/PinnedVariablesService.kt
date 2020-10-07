package strongdmm.service.pinned_variables

import strongdmm.application.Service
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Provider
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerPinnedVariablesService
import strongdmm.event.type.service.TriggerSettingsService
import strongdmm.service.settings.Settings

class PinnedVariablesService : Service {
    init {
        EventBus.sign(Provider.SettingsServiceSettings::class.java, ::handleProviderSettingsServiceSettings)
        EventBus.sign(TriggerPinnedVariablesService.FetchPinnedVariables::class.java, ::handleFetchPinnedVariables)
        EventBus.sign(TriggerPinnedVariablesService.PinVariable::class.java, ::handlePinVariables)
        EventBus.sign(TriggerPinnedVariablesService.UnpinVariable::class.java, ::handleUnpinVariable)
    }

    private lateinit var pinnedVariablesServiceSettings: PinnedVariablesServiceSettings

    private fun handleProviderSettingsServiceSettings(event: Event<Settings, Unit>) {
        pinnedVariablesServiceSettings = event.body.pinnedVariablesServiceSettings
    }

    private fun handleFetchPinnedVariables(event: Event<Unit, Set<String>>) {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment {
            event.reply(pinnedVariablesServiceSettings.pinnedVariables.getOrPut(it.absEnvPath) { mutableSetOf() })
        })
    }

    private fun handlePinVariables(event: Event<String, Unit>) {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment {
            pinnedVariablesServiceSettings.pinnedVariables.getOrPut(it.absEnvPath) { mutableSetOf() }.add(event.body)
            EventBus.post(TriggerSettingsService.SaveSettings())
        })
    }

    private fun handleUnpinVariable(event: Event<String, Unit>) {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment {
            pinnedVariablesServiceSettings.pinnedVariables[it.absEnvPath]?.remove(event.body)
            EventBus.post(TriggerSettingsService.SaveSettings())
        })
    }
}
