package io.github.spair.strongdmm.common.extension

import gnu.trove.map.hash.TFloatObjectHashMap
import gnu.trove.map.hash.TIntIntHashMap
import gnu.trove.map.hash.TIntObjectHashMap

inline fun <T> TFloatObjectHashMap<T>.getOrPut(key: Float, obj: (Float) -> T): T {
    if (!containsKey(key)) {
        put(key, obj(key))
    }
    return this[key]
}

inline fun <T> TIntObjectHashMap<T>.getOrPut(key: Int, obj: (Int) -> T): T {
    if (!containsKey(key)) {
        put(key, obj(key))
    }
    return this[key]
}

inline fun TIntIntHashMap.getOrPut(key: Int, obj: (Int) -> Int): Int {
    if (!containsKey(key)) {
        put(key, obj(key))
    }
    return this[key]
}
