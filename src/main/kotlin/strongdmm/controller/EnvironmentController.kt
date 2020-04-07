package strongdmm.controller

import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.SdmmParser
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerEnvironmentController
import java.io.File
import kotlin.concurrent.thread

class EnvironmentController : EventSender, EventConsumer {
    private lateinit var environment: Dme

    init {
        consumeEvent(TriggerEnvironmentController.OpenEnvironment::class.java, ::handleOpenEnvironment)
        consumeEvent(TriggerEnvironmentController.FetchOpenedEnvironment::class.java, ::handleFetchOpenedEnvironment)
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
