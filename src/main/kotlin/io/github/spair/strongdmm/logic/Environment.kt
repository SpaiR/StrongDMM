package io.github.spair.strongdmm.logic

import io.github.spair.dmm.io.reader.DmmReader
import io.github.spair.strongdmm.diDirectAll
import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.dme.Dme
import io.github.spair.strongdmm.logic.dme.parseDme
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.map.Dmm
import java.io.File

object Environment {

    private val objectTreeView by diInstance<ObjectTreeView>()
    private val mapCanvasView by diInstance<MapCanvasView>()

    lateinit var dme: Dme
    lateinit var absoluteRootPath: String
    lateinit var availableMaps: List<String>

    fun parseAndPrepareEnv(dmeFile: File) {
        if (Environment::dme.isInitialized) {
            History.clearActions()
            diDirectAll<EnvCleanable>().forEach(EnvCleanable::clean)
        }

        val s = System.currentTimeMillis()

        dme = parseDme(dmeFile.absolutePath)
        absoluteRootPath = dmeFile.parentFile.absolutePath
        findAvailableMaps(dmeFile.parentFile)
        objectTreeView.populateTree(dme)

        System.gc()

        println(System.currentTimeMillis() - s)
    }

    fun openMap(mapFile: File) {
        val dmmData = DmmReader.readMap(mapFile)
        val dmm = Dmm(mapFile.path, dmmData, dme)
        mapCanvasView.openMap(dmm)
    }

    fun openMap(mapPath: String) {
        openMap(File(mapPath))
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
}
