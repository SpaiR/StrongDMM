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
        consumeEvent(EventEnvironmentController.Open::class.java, ::handleOpen)
        consumeEvent(EventEnvironmentController.Fetch::class.java, ::handleFetch)
    }

    private fun handleOpen(event: Event<File, EnvironmentBlockStatus>) {
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

    private fun handleFetch(event: Event<Unit, Dme>) {
        if (this::environment.isInitialized) {
            event.reply(environment)
        }
    }
}
