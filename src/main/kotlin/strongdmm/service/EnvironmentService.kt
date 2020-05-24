package strongdmm.service

import strongdmm.Service
import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.SdmmParser
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerMapHolderService
import java.io.File
import kotlin.concurrent.thread

class EnvironmentService : Service, EventHandler {
    private lateinit var environment: Dme

    init {
        consumeEvent(TriggerEnvironmentService.OpenEnvironment::class.java, ::handleOpenEnvironment)
        consumeEvent(TriggerEnvironmentService.FetchOpenedEnvironment::class.java, ::handleFetchOpenedEnvironment)
    }

    private fun openEnvironment(event: Event<File, Unit>) {
        sendEvent(Reaction.EnvironmentReset())

        GlobalDmiHolder.resetEnvironment()
        GlobalTileItemHolder.resetEnvironment()

        thread(start = true) {
            sendEvent(Reaction.EnvironmentLoadStarted(event.body))

            environment = SdmmParser().parseDme(event.body)

            GlobalDmiHolder.environmentRootPath = environment.absRootDirPath
            GlobalTileItemHolder.environment = environment

            System.gc()

            sendEvent(Reaction.EnvironmentChanged(environment))
            sendEvent(Reaction.EnvironmentLoadStopped(true))

            event.reply(Unit)
        }
    }

    private fun handleOpenEnvironment(event: Event<File, Unit>) {
        if (this::environment.isInitialized) {
            sendEvent(TriggerMapHolderService.CloseAllMaps {
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
