package io.github.spair.strongdmm.logic.render

import java.util.TreeMap

typealias RenderInstances = TreeMap<Float, TreeMap<Float, MutableList<Long>>>

fun RenderInstances.get(plane: Float, layer: Float): MutableList<Long> {
    return computeIfAbsent(plane) { TreeMap() }.computeIfAbsent(layer) { mutableListOf() }
}
