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

    private fun openEnvironment(file: File) {
        sendEvent(Reaction.EnvironmentReset())

        GlobalDmiHolder.resetEnvironment()
        GlobalTileItemHolder.resetEnvironment()

        thread(start = true) {
            sendEvent(Reaction.EnvironmentLoadStarted(file))

            environment = SdmmParser().parseDme(file)

            GlobalDmiHolder.environmentRootPath = environment.absRootDirPath
            GlobalTileItemHolder.environment = environment

            System.gc()

            sendEvent(Reaction.EnvironmentChanged(environment))
            sendEvent(Reaction.EnvironmentLoadStopped(true))
        }
    }

    private fun handleOpenEnvironment(event: Event<File, Unit>) {
        if (this::environment.isInitialized) {
            sendEvent(TriggerMapHolderService.CloseAllMaps {
                if (it) {
                    openEnvironment(event.body)
                }
            })
        } else {
            openEnvironment(event.body)
        }
    }

    private fun handleFetchOpenedEnvironment(event: Event<Unit, Dme>) {
        if (this::environment.isInitialized) {
            event.reply(environment)
        }
    }
}
