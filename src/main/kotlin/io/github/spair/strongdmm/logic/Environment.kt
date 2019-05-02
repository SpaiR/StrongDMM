package io.github.spair.strongdmm.logic

import io.github.spair.dmm.io.reader.DmmReader
import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.instancelist.InstanceListController
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasController
import io.github.spair.strongdmm.gui.objtree.ObjectTreeController
import io.github.spair.strongdmm.logic.dme.Dme
import io.github.spair.strongdmm.logic.dme.parseDme
import io.github.spair.strongdmm.logic.map.Dmm
import java.io.File

object Environment {

    lateinit var dme: Dme
    lateinit var absoluteRootPath: String
    
    val availableMaps = mutableListOf<String>()

    private val objectTreeController by diInstance<ObjectTreeController>()
    private val mapCanvasController by diInstance<MapCanvasController>()
    private val instanceListController by diInstance<InstanceListController>()

    fun parseAndPrepareEnv(dmeFile: File): Dme {
        val s = System.currentTimeMillis()

        dme = parseDme(dmeFile.absolutePath)

        absoluteRootPath = dmeFile.parentFile.absolutePath
        objectTreeController.populateTree(dme)
        findAvailableMaps(dmeFile.parentFile)
        System.gc()

        println(System.currentTimeMillis() - s)

        return dme
    }

    fun openMap(mapFile: File) {
        val dmmData = DmmReader.readMap(mapFile)
        val dmm = Dmm(mapFile.path, dmmData, dme)
        mapCanvasController.openMap(dmm)
        instanceListController.updateSelectedInstanceInfo()
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
