package strongdmm.controller

import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.SdmmParser
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.EnvironmentBlockStatus
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.EventEnvironmentController
import java.io.File
import kotlin.concurrent.thread

class EnvironmentController : EventSender, EventConsumer {
    private lateinit var environment: Dme

    init {
        consumeEvent(EventEnvironmentController.OpenEnvironment::class.java, ::handleOpenEnvironment)
        consumeEvent(EventEnvironmentController.FetchOpenedEnvironment::class.java, ::handleFetchOpenedEnvironment)
    }

    private fun handleOpenEnvironment(event: Event<File, EnvironmentBlockStatus>) {
        sendEvent(EventGlobal.EnvironmentReset())

        GlobalDmiHolder.resetEnvironment()
        GlobalTileItemHolder.resetEnvironment()

        thread(start = true) {
            environment = SdmmParser().parseDme(event.body)

            GlobalDmiHolder.environmentRootPath = environment.rootPath
            GlobalTileItemHolder.environment = environment

            System.gc()

            event.reply(true)
            sendEvent(EventGlobal.EnvironmentChanged(environment))
        }
    }

    private fun handleFetchOpenedEnvironment(event: Event<Unit, Dme>) {
        if (this::environment.isInitialized) {
            event.reply(environment)
        }
    }
}
