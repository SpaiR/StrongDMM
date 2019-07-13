package io.github.spair.strongdmm.logic.dme

import io.github.spair.strongdmm.common.TYPE_WORLD
import io.github.spair.strongdmm.common.VAR_AREA
import io.github.spair.strongdmm.common.VAR_TURF

class Dme(val path: String, private val dmeItems: Map<String, DmeItem?>) {

    private lateinit var basicTurfType: String
    private lateinit var basicAreaType: String

    fun postInit() {
        val world = getItem(TYPE_WORLD)!!
        basicTurfType = world.getVar(VAR_TURF)!!
        basicAreaType = world.getVar(VAR_AREA)!!
    }

    fun getItem(type: String) = dmeItems[type]

    fun getBasicTurfType(): String = basicTurfType
    fun getBasicAreaType(): String = basicAreaType
}
