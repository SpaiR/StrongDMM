package io.github.spair.strongdmm.logic.dme

import io.github.spair.strongdmm.logic.EnvCleanable

object IsTypeCache : EnvCleanable {

    private val cache = mutableMapOf<String, Boolean>()

    fun isType(t1: String, t2: String) = cache.computeIfAbsent("$t1-$t2") { t1.startsWith(t2) }

    override fun clean() {
        cache.clear()
    }
}

// This 'isType' doesn't handle datum/atom and so on, since map editor doesn't place those types on the map,
// so additional checks would result into obsolete overhead.
fun isType(t1: String, t2: String) = IsTypeCache.isType(t1, t2)
