package strongdmm.service

import strongdmm.application.Service
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.SdmmParser
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ReactionEnvironmentService
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerMapHolderService
import java.io.File
import kotlin.concurrent.thread

class EnvironmentService : Service {
    private lateinit var environment: Dme

    init {
        EventBus.sign(TriggerEnvironmentService.OpenEnvironment::class.java, ::handleOpenEnvironment)
        EventBus.sign(TriggerEnvironmentService.FetchOpenedEnvironment::class.java, ::handleFetchOpenedEnvironment)
    }

    private fun openEnvironment(event: Event<File, Unit>) {
        EventBus.post(ReactionEnvironmentService.EnvironmentReset.SIGNAL)

        GlobalDmiHolder.resetEnvironment()
        GlobalTileItemHolder.resetEnvironment()

        thread(start = true) {
            EventBus.post(ReactionEnvironmentService.EnvironmentLoadStarted(event.body))

            environment = SdmmParser().parseDme(event.body)

            GlobalDmiHolder.environmentRootPath = environment.absRootDirPath
            GlobalTileItemHolder.environment = environment

            System.gc()

            EventBus.post(ReactionEnvironmentService.EnvironmentChanged(environment))
            EventBus.post(ReactionEnvironmentService.EnvironmentLoadStopped(true))

            event.reply(Unit)
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
