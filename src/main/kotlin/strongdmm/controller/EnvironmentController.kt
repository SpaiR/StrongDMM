package strongdmm.controller

import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.SdmmParser
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.Message
import kotlin.concurrent.thread

class EnvironmentController : EventSender, EventConsumer {
    private lateinit var environment: Dme

    init {
        consumeEvent(Event.ENVIRONMENT_OPEN, ::handleOpen)
        consumeEvent(Event.ENVIRONMENT_FETCH, ::handleFetch)
    }

    private fun handleOpen(msg: Message<String, Boolean>) {
        sendEvent(Event.GLOBAL_RESET_ENVIRONMENT)

        GlobalDmiHolder.resetEnvironment()
        GlobalTileItemHolder.resetEnvironment()

        thread(start = true) {
            environment = SdmmParser().parseDme(msg.body)

            GlobalDmiHolder.environmentRootPath = environment.rootPath
            GlobalTileItemHolder.environment = environment

            System.gc()

            msg.reply(true)
            sendEvent(Event.GLOBAL_SWITCH_ENVIRONMENT, environment)
        }
    }

    private fun handleFetch(msg: Message<Unit, Dme>) {
        msg.reply(environment)
    }
}
