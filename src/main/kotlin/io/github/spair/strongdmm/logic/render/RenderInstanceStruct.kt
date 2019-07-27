package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.common.DEFAULT_ICON_SIZE
import sun.misc.Unsafe

// This class is the mostly used class during rendering flow and here it's represented as a raw struct data.
// Thus by doing all allocations and de-allocations manually we save a lot on GC activity.
object RenderInstanceStruct {

    // Number of bytes for int32/float32 primitive type.
    private const val SIZE: Int = 4
    private const val FULL_STRUCT_CAPACITY: Long = SIZE * 14L // allocation size for 14 members

    // Bytes offset for all (14) struct members.
    private const val LOC_X_POS = SIZE * 0
    private const val LOC_Y_POS = SIZE * 1
    private const val TEXTURE_ID_POS = SIZE * 2
    private const val U1_POS = SIZE * 3
    private const val V1_POS = SIZE * 4
    private const val U2_POS = SIZE * 5
    private const val V2_POS = SIZE * 6
    private const val WIDTH_POS = SIZE * 7
    private const val HEIGHT_POS = SIZE * 8
    private const val TILE_ITEM_ID_POS = SIZE * 9
    private const val COLOR_RED_POS = SIZE * 10
    private const val COLOR_GREEN_POS = SIZE * 11
    private const val COLOR_BLUE_POS = SIZE * 12
    private const val COLOR_ALPHA_POS = SIZE * 13

    private val UNSAFE = getUnsafe()

    fun allocate(): Long {
        return UNSAFE.allocateMemory(FULL_STRUCT_CAPACITY)
    }

    fun deallocate(address: Long) {
        UNSAFE.freeMemory(address)
    }

    fun getLocX(address: Long): Float = UNSAFE.getFloat(address + LOC_X_POS)
    fun getLocY(address: Long): Float = UNSAFE.getFloat(address + LOC_Y_POS)
    fun getTextureId(address: Long): Int = UNSAFE.getInt(address + TEXTURE_ID_POS)
    fun getU1(address: Long): Float = UNSAFE.getFloat(address + U1_POS)
    fun getV1(address: Long): Float = UNSAFE.getFloat(address + V1_POS)
    fun getU2(address: Long): Float = UNSAFE.getFloat(address + U2_POS)
    fun getV2(address: Long): Float = UNSAFE.getFloat(address + V2_POS)
    fun getWidth(address: Long): Int = UNSAFE.getInt(address + WIDTH_POS)
    fun getHeight(address: Long): Int = UNSAFE.getInt(address + HEIGHT_POS)
    fun getTileItemId(address: Long): Int = UNSAFE.getInt(address + TILE_ITEM_ID_POS)
    fun getColorRed(address: Long): Float = UNSAFE.getFloat(address + COLOR_RED_POS)
    fun getColorGreen(address: Long): Float = UNSAFE.getFloat(address + COLOR_GREEN_POS)
    fun getColorBlue(address: Long): Float = UNSAFE.getFloat(address + COLOR_BLUE_POS)
    fun getColorAlpha(address: Long): Float = UNSAFE.getFloat(address + COLOR_ALPHA_POS)

    fun setMajor(
        address: Long,
        locX: Float = 0f,
        locY: Float = 0f,
        textureId: Int = 0,
        u1: Float = 0f,
        v1: Float = 0f,
        u2: Float = 1f,
        v2: Float = 1f,
        width: Int = DEFAULT_ICON_SIZE,
        height: Int = DEFAULT_ICON_SIZE,
        tileItemID: Int = 0
    ) {
        with(UNSAFE) {
            putFloat(address + LOC_X_POS, locX)
            putFloat(address + LOC_Y_POS, locY)

            putInt(address + TEXTURE_ID_POS, textureId)

            putFloat(address + U1_POS, u1)
            putFloat(address + V1_POS, v1)
            putFloat(address + U2_POS, u2)
            putFloat(address + V2_POS, v2)

            putInt(address + WIDTH_POS, width)
            putInt(address + HEIGHT_POS, height)

            putInt(address + TILE_ITEM_ID_POS, tileItemID)
        }
    }

    fun setColor(
        address: Long,
        colorRed: Float = 1f,
        colorGreen: Float = 1f,
        colorBlue: Float = 1f,
        colorAlpha: Float = 1f
    ) {
        with(UNSAFE) {
            putFloat(address + COLOR_RED_POS, colorRed)
            putFloat(address + COLOR_GREEN_POS, colorGreen)
            putFloat(address + COLOR_BLUE_POS, colorBlue)
            putFloat(address + COLOR_ALPHA_POS, colorAlpha)
        }
    }

    private fun getUnsafe(): Unsafe {
        val f = Unsafe::class.java.getDeclaredField("theUnsafe")
        f.isAccessible = true
        return f.get(null) as Unsafe
    }
}
