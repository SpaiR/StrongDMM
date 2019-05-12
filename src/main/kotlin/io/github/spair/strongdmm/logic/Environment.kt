package io.github.spair.strongdmm.logic

import io.github.spair.dmm.io.reader.DmmReader
import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.dme.Dme
import io.github.spair.strongdmm.logic.dme.parseDme
import io.github.spair.strongdmm.logic.map.Dmm
import java.io.File

object Environment {

    lateinit var dme: Dme
    lateinit var absoluteRootPath: String
    
    val availableMaps = mutableListOf<String>()

    private val objectTreeView by diInstance<ObjectTreeView>()
    private val mapCanvasView by diInstance<MapCanvasView>()
    private val instanceListView by diInstance<InstanceListView>()

    fun parseAndPrepareEnv(dmeFile: File) {
        val s = System.currentTimeMillis()

        dme = parseDme(dmeFile.absolutePath)

        absoluteRootPath = dmeFile.parentFile.absolutePath
        objectTreeView.populateTree(dme)
        findAvailableMaps(dmeFile.parentFile)
        System.gc()

        println(System.currentTimeMillis() - s)
    }

    fun openMap(mapFile: File) {
        val dmmData = DmmReader.readMap(mapFile)
        val dmm = Dmm(mapFile.path, dmmData, dme)
        mapCanvasView.openMap(dmm)
        instanceListView.updateSelectedInstanceInfo()
    }

    fun openMap(mapPath: String) {
        openMap(File(mapPath))
    }

    private fun findAvailableMaps(rootFolder: File) {
        rootFolder.walkTopDown().forEach {
            if (it.path.endsWith(".dmm")) {
                availableMaps.add(it.path)
            }
        }
    }
}
