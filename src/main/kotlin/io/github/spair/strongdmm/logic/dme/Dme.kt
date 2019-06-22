package io.github.spair.strongdmm.logic.dme

class Dme(private val dmeItems: Map<String, DmeItem?>) {
    fun getItem(type: String) = dmeItems[type]
}
