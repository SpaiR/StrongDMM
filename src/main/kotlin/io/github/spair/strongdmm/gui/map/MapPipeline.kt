package io.github.spair.strongdmm.gui.map

import io.github.spair.strongdmm.gui.map.input.KeyboardProcessor
import io.github.spair.strongdmm.gui.map.input.MouseProcessor
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import io.github.spair.strongdmm.logic.render.RenderInstanceStruct
import io.github.spair.strongdmm.logic.render.VisualComposer
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import java.awt.Canvas
import kotlin.concurrent.thread

class MapPipeline(private val canvas: Canvas) {

    private var glInitialized = false

    val openedMaps = linkedMapOf<Int, MapRenderData>()

    var selectedMapData: MapRenderData? = null
    var iconSize = 32

    // When true, all maps will have the same view coordinates
    var synchronizeMaps: Boolean = false

    // Coords of tile where the mouse is
    var xMouseMap = 0
    var yMouseMap = 0

    // Coords of pixel on the map where the mouse is
    var xMouse = 0f
    var yMouse = 0f

    // When true, while next rendering loop, top item under the mouse will be selected
    var isSelectItem = false

    init {
        MouseProcessor.mapPipeline = this
    }

    fun switchMap(hash: Int) {
        openedMaps[hash]?.let { switchMap(it.dmm) }
    }

    fun switchMap(map: Dmm) {
        val hash = map.hashCode()

        if (openedMaps.containsKey(hash)) {
            selectedMapData = openedMaps.getValue(hash)
        } else {
            val newMap = MapRenderData(map)
            openedMaps[hash] = newMap

            if (synchronizeMaps && selectedMapData != null) {
                triggerMapSync(selectedMapData!!)
            }

            selectedMapData = newMap
        }

        iconSize = map.iconSize

        if (!glInitialized) {
            initGLDisplay()
        }

        Frame.update(true)
    }

    fun closeMap(hash: Int) {
        if (!openedMaps.containsKey(hash)) {
            return
        }

        val mapData = openedMaps.getValue(hash)

        if (selectedMapData === mapData) {
            var selectedMapIndex = 0

            for ((index, mapHash) in openedMaps.keys.withIndex()) {
                if (mapHash == hash) {
                    selectedMapIndex = index
                    break
                }
            }

            if (openedMaps.size > 1) {
                val index = if (selectedMapIndex == 0) {
                    1
                } else {
                    if (openedMaps.size >= selectedMapIndex + 2) {
                        selectedMapIndex + 1
                    } else {
                        selectedMapIndex - 1
                    }
                }

                selectedMapData = openedMaps.values.toTypedArray()[index]
                Frame.update(true)
            } else {
                selectedMapData = null
            }
        }

        openedMaps.remove(hash)
    }

    fun triggerMapSync(selectedMap: MapRenderData) {
        openedMaps.values.forEach { openedMap ->
            if (openedMap !== selectedMap) {
                openedMap.xMapOff = selectedMap.xMapOff
                openedMap.yMapOff = selectedMap.yMapOff

                openedMap.viewZoom = selectedMap.viewZoom

                openedMap.xViewOff = selectedMap.xViewOff
                openedMap.yViewOff = selectedMap.yViewOff
            }
        }
    }

    private fun initGLDisplay() {
        thread(start = true) {
            glInitialized = true
            Display.setParent(canvas)
            Display.create()
            DmiProvider.initTextures()
            startRenderLoop()  // this is where the magic happens
            DmiProvider.clearTextures()
            VisualComposer.clearCache()
            Display.destroy()
            glInitialized = false
        }
    }

    private fun startRenderLoop() {
        glClearColor(0.25f, 0.25f, 0.5f , 1f)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        while (!Display.isCloseRequested() && selectedMapData != null) {
            if (Frame.hasUpdates()) {
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                val width = Display.getWidth()
                val height = Display.getHeight()

                glViewport(0, 0, width, height)

                glMatrixMode(GL_PROJECTION)
                glLoadIdentity()
                glOrtho(0.0, getViewWidth(), 0.0, getViewHeight(), 1.0, -1.0)
                glMatrixMode(GL_MODELVIEW)
                glLoadIdentity()
                glTranslatef(selectedMapData!!.xViewOff, selectedMapData!!.yViewOff, 0f)

                // actual rendering
                renderMap()
                renderMousePosition()
                SelectOperation.render(iconSize)

                Display.update(false)
            }

            Display.processMessages()
            KeyboardProcessor.fire()
            MouseProcessor.fire()
            Display.sync(30)
        }
    }

    private fun renderMap() {
        val horTilesNum = (getViewWidth() / iconSize + 0.5f).toInt()
        val verTilesNum = (getViewHeight() / iconSize + 0.5f).toInt()

        val dmm = selectedMapData!!.dmm
        val xMapOff = selectedMapData!!.xMapOff
        val yMapOff = selectedMapData!!.yMapOff

        val renderInstances = VisualComposer.composeFrame(dmm, xMapOff, yMapOff, horTilesNum, verTilesNum, Frame.isForced())
        var bindedTexture = -1

        glEnable(GL_TEXTURE_2D)

        renderInstances.values.forEach { plane ->
            plane.values.forEach { layer ->
                layer.forEach { riAddress ->
                    val colorRed = RenderInstanceStruct.getColorRed(riAddress)
                    val colorGreen = RenderInstanceStruct.getColorGreen(riAddress)
                    val colorBlue = RenderInstanceStruct.getColorBlue(riAddress)
                    val colorAlpha = RenderInstanceStruct.getColorAlpha(riAddress)

                    glColor4f(colorRed, colorGreen, colorBlue, colorAlpha)

                    val textureId = RenderInstanceStruct.getTextureId(riAddress)

                    if (textureId != bindedTexture) {
                        glBindTexture(GL_TEXTURE_2D, textureId)
                        bindedTexture = textureId
                    }

                    val locX = RenderInstanceStruct.getLocX(riAddress)
                    val locY = RenderInstanceStruct.getLocY(riAddress)

                    glPushMatrix()
                    glTranslatef(locX, locY, 0f)

                    val width = RenderInstanceStruct.getWidth(riAddress)
                    val height = RenderInstanceStruct.getHeight(riAddress)

                    val u1 = RenderInstanceStruct.getU1(riAddress)
                    val v1 = RenderInstanceStruct.getV1(riAddress)
                    val u2 = RenderInstanceStruct.getU2(riAddress)
                    val v2 = RenderInstanceStruct.getV2(riAddress)

                    glBegin(GL_QUADS)
                    run {
                        glTexCoord2f(u2, v1)
                        glVertex2i(width, height)

                        glTexCoord2f(u1, v1)
                        glVertex2i(0, height)

                        glTexCoord2f(u1, v2)
                        glVertex2i(0, 0)

                        glTexCoord2f(u2, v2)
                        glVertex2i(width, 0)
                    }
                    glEnd()

                    glPopMatrix()
                }
            }
        }

        glDisable(GL_TEXTURE_2D)

        // Postponed images will be loaded in next frame
        if (VisualComposer.hasIncompleteJob) {
            Frame.update()
        }

        if (isSelectItem) {
            isSelectItem = false
            findAndSelectItemUnderMouse(renderInstances)
        }
    }

    private fun renderMousePosition() {
        if (xMouseMap == OUT_OF_BOUNDS || yMouseMap == OUT_OF_BOUNDS) {
            return
        }

        val xPos = (xMouseMap - 1) * iconSize
        val yPos = (yMouseMap - 1) * iconSize

        glColor4f(1f, 1f, 1f, 0.25f)

        glBegin(GL_QUADS)
        run {
            glVertex2i(xPos, yPos)
            glVertex2i(xPos + iconSize, yPos)
            glVertex2i(xPos + iconSize, yPos + iconSize)
            glVertex2i(xPos, yPos + iconSize)
        }
        glEnd()
    }

    private fun getViewWidth() = Display.getWidth() * selectedMapData!!.viewZoom.toDouble()
    private fun getViewHeight() = Display.getHeight() * selectedMapData!!.viewZoom.toDouble()
}
