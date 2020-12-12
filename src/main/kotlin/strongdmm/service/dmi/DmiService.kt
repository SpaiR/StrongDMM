package strongdmm.service.dmi

import strongdmm.application.PostInitialize
import strongdmm.application.Service
import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ProviderDmiService
import strongdmm.event.service.ReactionEnvironmentService

class DmiService : Service, PostInitialize {
    private val dmiLoader = DmiLoader()
    private val dmiCache = DmiCache(dmiLoader)

    init {
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionEnvironmentService.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
    }

    override fun postInit() {
        EventBus.post(ProviderDmiService.DmiCache(dmiCache))
    }

    private fun handleEnvironmentReset() {
        dmiCache.reset()
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        dmiCache.init(event.body.absRootDirPath)
    }
}
