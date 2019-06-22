package io.github.spair.strongdmm.logic

import io.github.spair.dmm.io.reader.DmmReader
import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.StatusView
import io.github.spair.strongdmm.gui.TabbedMapPanelView
import io.github.spair.strongdmm.gui.TabbedObjectPanelView
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.dme.Dme
import io.github.spair.strongdmm.logic.dme.parseDme
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.TileItemProvider
import java.io.File
import kotlin.concurrent.thread

object Environment {

    lateinit var dme: Dme
    lateinit var absoluteRootPath: String
    lateinit var availableMaps: List<String>

    private val openedMaps = mutableSetOf<String>()

    fun parseAndPrepareEnv(dmeFile: File) {
        if (Environment::dme.isInitialized) {
            cleanEnvironmentResources()
        }

        val s = System.currentTimeMillis()

        dme = parseDme(dmeFile.absolutePath)
        absoluteRootPath = dmeFile.parentFile.absolutePath
        findAvailableMaps(dmeFile.parentFile)
        ObjectTreeView.populateTree(dme)

        System.gc()

        println(System.currentTimeMillis() - s)
    }

    fun openMap(mapFile: File) {
        if (!openedMaps.add(mapFile.path)) {
            return
        }

        val dmmData = DmmReader.readMap(mapFile)
        val dmm = Dmm(mapFile, dmmData, dme)

        StatusView.showLoader("Loading ${dmm.mapName}..")
        PrimaryFrame.isEnabled = false
        MapView.openMap(dmm)

        thread(start = true) {
            while (MapView.isMapLoadingInProcess()) {
                Thread.yield()
            }

            PrimaryFrame.isEnabled = true
            StatusView.hideLoader()

            TabbedMapPanelView.addMapTab(dmm)
            MenuBarView.updateUndoable()
        }
    }

    fun openMap(mapPath: String) {
        openMap(File(mapPath))
    }

    fun closeMap(dmm: Dmm) {
        openedMaps.remove(dmm.mapPath)
        MapView.closeMap(dmm.hashCode())
        History.clearUnusedActions(MapView.getOpenedMaps())
        MenuBarView.updateUndoable()
    }

    private fun findAvailableMaps(rootFolder: File) {
        val mapPaths = mutableListOf<String>()

        rootFolder.walkTopDown().forEach {
            if (it.path.endsWith(".dmm")) {
                mapPaths.add(it.path)
            }
        }

        availableMaps = mapPaths
    }

    private fun cleanEnvironmentResources() {
        openedMaps.clear()
        History.clean()
        MapView.clean()
        ObjectTreeView.clean()
        InstanceListView.clean()
        TabbedObjectPanelView.clean()
        TileItemProvider.clean()
        TabbedMapPanelView.clean()
    }
}
