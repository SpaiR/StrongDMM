package io.github.spair.strongdmm.logic.render

import gnu.trove.list.array.TLongArrayList
import gnu.trove.map.hash.TFloatObjectHashMap
import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.LayersManager
import io.github.spair.strongdmm.logic.map.TileItemProvider

object VisualComposer {

    // We will look for things outside of viewport range to handle big objects (bigger then /world/icon_size)
    private const val ADDITIONAL_VIEW_RANGE: Int = 2

    private var xMapOffPrev: Int = 0
    private var yMapOffPrev: Int = 0
    private var horTilesNumPrev: Int = 0
    private var verTilesNumPrev: Int = 0
    private var drawAreasBorderPrev: Boolean = true

    private var riCache: LongArray? = null

    val framedAreas: MutableList<FramedArea> = mutableListOf()

    fun clearCache() {
        deallocateCache()
        framedAreas.clear()
        riCache = null
    }

    fun composeFrame(
        dmm: Dmm,
        xMapOff: Int,
        yMapOff: Int,
        horTilesNum: Int,
        verTilesNum: Int,
        forceUpdate: Boolean,
        drawAreasBorder: Boolean
    ): LongArray {
        // Use cached render instances
        if (riCache != null &&
            !forceUpdate &&
            xMapOffPrev == xMapOff && yMapOffPrev == yMapOff &&
            horTilesNumPrev == horTilesNum && verTilesNumPrev == verTilesNum &&
            drawAreasBorderPrev == drawAreasBorder
        ) return riCache!!

        deallocateCache()
        framedAreas.clear()

        // val planeLayers = RenderInstances()
        val planeLayers = TFloatObjectHashMap<TFloatObjectHashMap<TLongArrayList>>()
        var itemCount = 0

        // Collect all items to self sorted map
        for (x in -ADDITIONAL_VIEW_RANGE until horTilesNum + ADDITIONAL_VIEW_RANGE) {
            for (y in -ADDITIONAL_VIEW_RANGE until verTilesNum + ADDITIONAL_VIEW_RANGE) {
                val tileX = xMapOff + x
                val tileY = yMapOff + y

                if (tileX < 1 || tileX > dmm.getMaxX() || tileY < 1 || tileY > dmm.getMaxY()) {
                    continue
                }

                val tile = dmm.getTile(tileX, tileY)

                val renderX = (tileX - 1) * dmm.iconSize
                val renderY = (tileY - 1) * dmm.iconSize

                var currentAreaType: String? = null

                // Collect render instances
                for (tileItemId in tile?.unsafeTileItemsIDs() ?: intArrayOf()) {
                    val tileItem = TileItemProvider.getByID(tileItemId)

                    if (drawAreasBorder && currentAreaType == null && tileItem.isType(TYPE_AREA)) {
                        currentAreaType = tileItem.type
                    }

                    if (LayersManager.isHiddenType(tileItem.type)) {
                        continue
                    }

                    if (!planeLayers.containsKey(tileItem.plane)) {
                        planeLayers.put(tileItem.plane, TFloatObjectHashMap())
                    }

                    val plane = planeLayers[tileItem.plane]

                    if (!plane.containsKey(tileItem.layer)) {
                        plane.put(tileItem.layer, TLongArrayList())
                    }

                    val layer = plane[tileItem.layer]

                    layer.add(RenderInstanceProvider.allocateRenderInstance(renderX.toFloat(), renderY.toFloat(), tileItem))
                    itemCount++
                }

                // Collect data to draw areas borders
                if (drawAreasBorder) {
                    var dir = 0

                    if (isFramedBorder(dmm, tileX - 1, tileY, currentAreaType)) dir = dir or WEST
                    if (isFramedBorder(dmm, tileX + 1, tileY, currentAreaType)) dir = dir or EAST
                    if (isFramedBorder(dmm, tileX, tileY - 1, currentAreaType)) dir = dir or SOUTH
                    if (isFramedBorder(dmm, tileX, tileY + 1, currentAreaType)) dir = dir or NORTH

                    if (dir != 0) {
                        framedAreas.add(FramedArea(renderX, renderY, dir))
                    }
                }
            }
        }

        val sortedItemsIDs = LongArray(itemCount)
        var index = 0

        // Sort items on the same layer
        for (plane in planeLayers.keys().sortedArray()) {
            for (layer in planeLayers[plane].keys().sortedArray()) {
                val itemsIDs = planeLayers[plane][layer].toArray()
                mergeSort(itemsIDs)
                for (itemID in itemsIDs) {
                    sortedItemsIDs[index++] = itemID
                }
            }
        }

        riCache = sortedItemsIDs
        xMapOffPrev = xMapOff
        yMapOffPrev = yMapOff
        horTilesNumPrev = horTilesNum
        verTilesNumPrev = verTilesNum
        drawAreasBorderPrev = drawAreasBorder

        return sortedItemsIDs
    }

    private fun isFramedBorder(dmm: Dmm, x: Int, y: Int, currentAreaType: String?): Boolean {
        dmm.getTile(x, y)?.let { tile ->
            tile.getTileItems().find { it.isType(TYPE_AREA) }?.let { area ->
                return currentAreaType != area.type
            }
        }
        return false
    }

    private fun deallocateCache() {
        riCache?.forEach { riAddress ->
            RenderInstanceStruct.deallocate(riAddress)
        }
    }

    // Non-generalized function to do merge sort
    // Code itself, with small modifications, was taken from here: https://github.com/JetBrains/kotlin/blob/142c9e2a8bf72b6d662d8f302faeab890053dad0/libraries/stdlib/js/src/kotlin/collections/ArraySorting.kt
    private fun mergeSort(array: LongArray) {
        val buffer = LongArray(array.size)
        val result = mergeSort(array, buffer, 0, array.lastIndex)
        if (result !== array) {
            for (i in 0 until result.size) {
                array[i] = result[i]
            }
        }
    }

    // Both start and end are inclusive indices.
    private fun mergeSort(array: LongArray, buffer: LongArray, start: Int, end: Int): LongArray {
        if (start == end) {
            return array
        }

        val median = (start + end) / 2
        val left = mergeSort(array, buffer, start, median)
        val right = mergeSort(array, buffer, median + 1, end)

        val target = if (left === buffer) array else buffer

        // Merge.
        var leftIndex = start
        var rightIndex = median + 1
        for (i in start..end) {
            when {
                leftIndex <= median && rightIndex <= end -> {
                    val leftValue = left[leftIndex]
                    val rightValue = right[rightIndex]

                    if (RenderComparator.compare(leftValue, rightValue) <= 0) {
                        target[i] = leftValue
                        leftIndex++
                    } else {
                        target[i] = rightValue
                        rightIndex++
                    }
                }
                leftIndex <= median -> {
                    target[i] = left[leftIndex]
                    leftIndex++
                }
                else /* rightIndex <= end */ -> {
                    target[i] = right[rightIndex]
                    rightIndex++
                    Unit
                }
            }
        }

        return target
    }
}
