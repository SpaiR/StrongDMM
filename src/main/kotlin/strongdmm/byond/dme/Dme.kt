package strongdmm.byond.dme

import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_AREA
import strongdmm.byond.VAR_TURF

class Dme(
    val rootPath: String,
    private val dmeItems: Map<String, DmeItem>
) {
    private lateinit var basicTurfType: String
    private lateinit var basicAreaType: String

    fun postInit() {
        val world = getItem(TYPE_WORLD)!!
        basicTurfType = world.getVar(VAR_TURF)!!
        basicAreaType = world.getVar(VAR_AREA)!!
    }

    fun getItem(type: String): DmeItem? = dmeItems[type]

    fun getBasicTurfType(): String = basicTurfType
    fun getBasicAreaType(): String = basicAreaType
}
