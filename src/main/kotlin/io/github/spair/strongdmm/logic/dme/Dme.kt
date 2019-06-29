package io.github.spair.strongdmm.logic.dme

class Dme(val path: String, private val dmeItems: Map<String, DmeItem?>) {
    fun getItem(type: String) = dmeItems[type]
}
