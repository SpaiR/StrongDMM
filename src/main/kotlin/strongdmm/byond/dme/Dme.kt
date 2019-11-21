package strongdmm.byond.dme

import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_AREA
import strongdmm.byond.VAR_TURF

class Dme(
    val rootPath: String,
    private val dmeItems: Map<String, DmeItem>
) {
    lateinit var basicTurfType: String
        private set
    lateinit var basicAreaType: String
        private set

    fun postInit() {
        val world = getItem(TYPE_WORLD)!!
        basicTurfType = world.getVar(VAR_TURF)!!
        basicAreaType = world.getVar(VAR_AREA)!!
    }

    fun getItem(type: String): DmeItem? = dmeItems[type]
}
