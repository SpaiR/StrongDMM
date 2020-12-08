package strongdmm.service

import strongdmm.application.Processable
import strongdmm.application.Service
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.SdmmParser
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ReactionEnvironmentService
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerMapHolderService
import java.io.File
import kotlin.concurrent.thread

class EnvironmentService : Service, Processable {
    private lateinit var environment: Dme
    private var isEnvironmentWasLoaded: Boolean = false

    init {
        EventBus.sign(TriggerEnvironmentService.OpenEnvironment::class.java, ::handleOpenEnvironment)
        EventBus.sign(TriggerEnvironmentService.FetchOpenedEnvironment::class.java, ::handleFetchOpenedEnvironment)
    }

    override fun process() {
        if (isEnvironmentWasLoaded) {
            isEnvironmentWasLoaded = false
            System.gc()
            GlobalTileItemHolder.environment = environment
            EventBus.post(ReactionEnvironmentService.EnvironmentChanged(environment))
            EventBus.post(ReactionEnvironmentService.EnvironmentOpened())
        }
    }

    private fun openEnvironment(event: Event<File, Unit>) {
        EventBus.post(ReactionEnvironmentService.EnvironmentReset.SIGNAL)

        GlobalTileItemHolder.resetEnvironment()

        thread(start = true) {
            EventBus.post(ReactionEnvironmentService.EnvironmentLoadStarted(event.body))
            environment = SdmmParser().parseDme(event.body)
            EventBus.post(ReactionEnvironmentService.EnvironmentLoadStopped(true))
            isEnvironmentWasLoaded = true
        }
    }

    private fun handleOpenEnvironment(event: Event<File, Unit>) {
        if (this::environment.isInitialized) {
            EventBus.post(TriggerMapHolderService.CloseAllMaps {
                if (it) {
                    openEnvironment(event)
                }
            })
        } else {
            openEnvironment(event)
        }
    }

    private fun handleFetchOpenedEnvironment(event: Event<Unit, Dme>) {
        if (this::environment.isInitialized) {
            event.reply(environment)
        }
    }
}
