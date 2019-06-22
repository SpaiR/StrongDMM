package io.github.spair.strongdmm.common

// This 'isType' doesn't handle datum/atom and so on, since map editor doesn't place those types on the map,
// so additional checks would result into obsolete overhead.
fun isType(t1: String, t2: String) = t1.startsWith(t2)
