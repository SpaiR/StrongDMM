package strongdmm.service

import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.SdmmParser
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerEnvironmentService
import java.io.File
import kotlin.concurrent.thread

class EnvironmentService : EventHandler {
    private lateinit var environment: Dme

    init {
        consumeEvent(TriggerEnvironmentService.OpenEnvironment::class.java, ::handleOpenEnvironment)
        consumeEvent(TriggerEnvironmentService.FetchOpenedEnvironment::class.java, ::handleFetchOpenedEnvironment)
    }

    private fun handleOpenEnvironment(event: Event<File, Unit>) {
        sendEvent(Reaction.EnvironmentReset())

        GlobalDmiHolder.resetEnvironment()
        GlobalTileItemHolder.resetEnvironment()

        thread(start = true) {
            sendEvent(Reaction.EnvironmentLoading(event.body))

            environment = SdmmParser().parseDme(event.body)

            GlobalDmiHolder.environmentRootPath = environment.absRootDirPath
            GlobalTileItemHolder.environment = environment

            System.gc()

            sendEvent(Reaction.EnvironmentChanged(environment))
            sendEvent(Reaction.EnvironmentLoaded(true))
        }
    }

    private fun handleFetchOpenedEnvironment(event: Event<Unit, Dme>) {
        if (this::environment.isInitialized) {
            event.reply(environment)
        }
    }
}
