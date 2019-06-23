package io.github.spair.strongdmm.gui.map

import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.gui.StatusView
import io.github.spair.strongdmm.gui.map.input.KeyboardProcessor
import io.github.spair.strongdmm.gui.map.input.MouseProcessor
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.render.RenderInstanceProvider
import io.github.spair.strongdmm.logic.render.RenderInstanceStruct
import io.github.spair.strongdmm.logic.render.VisualComposer
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import java.awt.Canvas
import kotlin.concurrent.thread

class MapPipeline(private val canvas: Canvas) {

    var selectedMapData: MapRenderData? = null
    var iconSize = DEFAULT_ICON_SIZE
    val openedMaps: MutableMap<Int, MapRenderData> = mutableMapOf()

    // Marks that our OpenGL context is initialized.
    private var glInitialized: Boolean = false

    // When true, we are in a process of opening new map.
    // That means that we are loading textures for all objects on the map.
    // Volatile because it's accessed from differed threads.
    @Volatile var mapLoadingInProcess = false

    // When true, all opened maps will have the same view coordinates
    var synchronizeMaps: Boolean = false

    // When true, all areas borders will be visible
    var drawAreasBorder: Boolean = true

    // Coords of tile where the mouse is
    var xMouseMap = 0
    var yMouseMap = 0

    // Coords of pixel on the map where the mouse is
    var xMouse = 0f
    var yMouse = 0f

    // When true, while next rendering loop, top item under the mouse will be selected
    var isSelectItem: Boolean = false

    init {
        // some ugly shit here...
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
                syncOpenedMaps(selectedMapData!!)
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

        // Do some clean up work if we are closing selected map
        if (selectedMapData === mapData) {
            var selectedMapIndex = 0

            for ((index, mapHash) in openedMaps.keys.withIndex()) {
                if (mapHash == hash) {
                    selectedMapIndex = index
                    break
                }
            }

            // Switch selected map to the one which is on the left side, or right if no other. Do nothing if no other maps.
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

    fun syncOpenedMaps(selectedMap: MapRenderData) {
        openedMaps.values.forEach { openedMap ->
            if (openedMap !== selectedMap) {
                openedMap.xMapOff = selectedMap.xMapOff
                openedMap.yMapOff = selectedMap.yMapOff

                openedMap.viewZoom = selectedMap.viewZoom
                openedMap.currZoom = selectedMap.currZoom

                openedMap.xViewOff = selectedMap.xViewOff
                openedMap.yViewOff = selectedMap.yViewOff
            }
        }
    }

    private fun initGLDisplay() {
        thread(start = true) {
            // OpenGL initialization
            glInitialized = true
            Display.setParent(canvas)
            Display.create()
            DmiProvider.initTextures()

            // This is where the magic happens
            startRenderLoop()

            // Clean environment after all maps were closed
            DmiProvider.clearTextures()
            RenderInstanceProvider.clearTextures()
            VisualComposer.clearCache()
            StatusView.updateCoords(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

            // OpenGL destroying
            Display.destroy()
            glInitialized = false
        }
    }

    private fun startRenderLoop() {
        glClearColor(0.25f, 0.25f, 0.5f, 1f)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        while (!Display.isCloseRequested() && selectedMapData != null) {
            if (mapLoadingInProcess) {
                RenderInstanceProvider.loadMapIcons(selectedMapData!!.dmm)
                mapLoadingInProcess = false
            }

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

                // Actual rendering
                renderMap()
                renderMousePosition()
                SelectOperation.render(iconSize)

                Display.update(false)
            }

            // Handle user input
            Display.processMessages()
            KeyboardProcessor.fire()
            MouseProcessor.fire()

            // 30 fps looks smooth enough and at the same time it consumes less resources
            Display.sync(30)
        }
    }

    private fun renderMap() {
        val horTilesNum = (getViewWidth() / iconSize + 0.5f).toInt()
        val verTilesNum = (getViewHeight() / iconSize + 0.5f).toInt()

        val dmm = selectedMapData!!.dmm
        val xMapOff = selectedMapData!!.xMapOff
        val yMapOff = selectedMapData!!.yMapOff

        var bindedTexture = -1
        val renderInstances = VisualComposer.composeFrame(
            dmm, xMapOff, yMapOff, horTilesNum, verTilesNum, Frame.isForced(), drawAreasBorder
        )

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

        if (isSelectItem) {
            isSelectItem = false
            findAndSelectItemUnderMouse(renderInstances)
        }

        if (drawAreasBorder) {
            renderAreasBorder()
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

    private fun renderAreasBorder() {
        glColor4f(0.8f, 0.8f, 0.8f, 1f)

        glLineWidth(1.5f)
        glBegin(GL_LINES)

        VisualComposer.framedAreas.forEach { framedArea ->
            val x = framedArea.x
            val y = framedArea.y
            val dir = framedArea.dir

            if ((dir and WEST) != 0) {
                glVertex2i(x, y)
                glVertex2i(x, y + iconSize)
            }
            if ((dir and EAST) != 0) {
                glVertex2i(x + iconSize, y)
                glVertex2i(x + iconSize, y + iconSize)
            }
            if ((dir and SOUTH) != 0) {
                glVertex2i(x, y)
                glVertex2i(x + iconSize, y)
            }
            if ((dir and NORTH) != 0) {
                glVertex2i(x, y + iconSize)
                glVertex2i(x + iconSize, y + iconSize)
            }
        }

        glEnd()
        glLineWidth(1f)
    }

    // Calculate and return actual view width with respect of zoom.
    private fun getViewWidth(): Double = Display.getWidth() * selectedMapData!!.viewZoom.toDouble()
    private fun getViewHeight(): Double = Display.getHeight() * selectedMapData!!.viewZoom.toDouble()
}
