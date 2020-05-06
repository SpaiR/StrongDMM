package strongdmm.byond.dme

import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_AREA
import strongdmm.byond.VAR_ICON_SIZE
import strongdmm.byond.VAR_TURF
import strongdmm.util.DEFAULT_ICON_SIZE

class Dme(
    val name: String,
    val absRootDirPath: String,
    val absEnvPath: String,
    val items: Map<String, DmeItem>
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

    fun getItem(type: String): DmeItem? = items[type]

    fun getWorldIconSize(): Int = getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: DEFAULT_ICON_SIZE
}
