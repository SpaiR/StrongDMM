package strongdmm.service

import strongdmm.PostInitialize
import strongdmm.Service
import strongdmm.StrongDMM
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.ui.TriggerChangelogPanelUi
import java.io.File

class ChangelogService : Service, EventHandler, PostInitialize {
    companion object {
        private val lastOpenedChangelogHashFile: File = File(StrongDMM.homeDir.toFile(), "loch.dat")
    }

    private val changelogText: String = this::class.java.classLoader.getResourceAsStream("CHANGELOG.txt")!!.use {
        it.readAllBytes().toString(Charsets.UTF_8)
    }

    override fun postInit() {
        sendEvent(Provider.ChangelogControllerChangelogText(changelogText))

        if (!lastOpenedChangelogHashFile.exists()) {
            lastOpenedChangelogHashFile.createNewFile()
        }

        val lastOpenedHash = lastOpenedChangelogHashFile.readText(Charsets.UTF_8).toIntOrNull() ?: 0
        val currentHash = changelogText.hashCode()

        if (lastOpenedHash != currentHash) {
            sendEvent(TriggerChangelogPanelUi.Open())
            lastOpenedChangelogHashFile.writeText(currentHash.toString())
        }
    }
}