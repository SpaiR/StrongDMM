package strongdmm.controller

import strongdmm.byond.dme.Dme
import strongdmm.byond.dme.SdmmParser
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.inline.AbsPath
import kotlin.concurrent.thread

class EnvironmentController : EventSender, EventConsumer {
    private lateinit var environment: Dme

    init {
        consumeEvent(Event.EnvironmentController.Open::class.java, ::handleOpen)
        consumeEvent(Event.EnvironmentController.Fetch::class.java, ::handleFetch)
    }

    private fun handleOpen(event: Event<AbsPath, Boolean>) {
        sendEvent(Event.Global.ResetEnvironment())

        GlobalDmiHolder.resetEnvironment()
        GlobalTileItemHolder.resetEnvironment()

        thread(start = true) {
            environment = SdmmParser().parseDme(event.body.value)

            GlobalDmiHolder.environmentRootPath = environment.rootPath
            GlobalTileItemHolder.environment = environment

            System.gc()

            event.reply(true)
            sendEvent(Event.Global.SwitchEnvironment(environment))
        }
    }

    private fun handleFetch(event: Event<Unit, Dme>) {
        event.reply(environment)
    }
}
