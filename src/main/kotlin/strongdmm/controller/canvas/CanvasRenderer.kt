package strongdmm.controller.canvas

import imgui.ImVec4
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30.*
import strongdmm.byond.dmi.IconSprite
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.controller.frame.FrameMesh
import strongdmm.util.DEFAULT_ICON_SIZE
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.imgui.GREEN_RGBA
import strongdmm.window.AppWindow
import java.nio.ByteBuffer

class CanvasRenderer {
    var redraw: Boolean = false
    var frameMeshes: List<FrameMesh> = emptyList()

    private val frameBuffer: Int = glGenFramebuffers()
    private var isTextureAttached: Boolean = false

    private var canvasTexture: Int = -1
    private var canvasTextureIsFilled: Boolean = false

    // Variables provided by CanvasController every cycle
    lateinit var renderData: RenderData
    var xMapMousePos: Int = OUT_OF_BOUNDS
    var yMapMousePos: Int = OUT_OF_BOUNDS
    var iconSize: Int = DEFAULT_ICON_SIZE
    var mousePosX: Float = 0f
    var mousePosY: Float = 0f

    // Used to visually emphasize attention on something on the map
    var markedPosition: MapPos? = null
    var selectedTiles: Collection<MapPos>? = null
    var selectedArea: MapArea? = null
    var highlightSelectedArea: Boolean = false

    var windowWidth: Int = -1
    var windowHeight: Int = -1

    // Used to handle tile item selection mode
    var isTileItemSelectMode: Boolean = false
    var tileItemSelectColor: ImVec4 = GREEN_RGBA
    var tileItemIdMouseOver: Long = 0
    private var pixelsBuffer: ByteBuffer = BufferUtils.createByteBuffer(512 * 512 * 4) // used to read item texture, could be resized to store more data
    private var markedTileItemLvl: Int = -1 // level of the marker item; marked means that the pixel under the mouse for this item is opaque

    fun render() {
        val windowWidth = AppWindow.windowWidth
        val windowHeight = AppWindow.windowHeight

        if (windowWidth == 0 && windowHeight == 0) {
            return
        }

        if (this.windowWidth != windowWidth || this.windowHeight != windowHeight || canvasTexture == -1) {
            this.windowWidth = windowWidth
            this.windowHeight = windowHeight
            createCanvasTexture()
        }

        if (canvasTextureIsFilled) {
            glViewport(0, 0, windowWidth, windowHeight)
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(0.0, windowWidth.toDouble(), 0.0, windowHeight.toDouble(), -1.0, 1.0)
            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()

            renderCanvasTexture()
            renderMousePosition()
            renderMarkedPosition()
            renderSelectedTiles()
            renderSelectedArea()

            if (!redraw && !isTileItemSelectMode) {
                return
            }
        }

        fillCanvasTexture()

        canvasTextureIsFilled = true
        redraw = false
    }

    fun invalidateCanvasTexture() {
        if (canvasTexture != -1) {
            glDeleteTextures(canvasTexture)
            canvasTexture = -1
            canvasTextureIsFilled = false
            isTextureAttached = false
        }
    }

    private fun renderCanvasTexture() {
        glColor4f(1f, 1f, 1f, 1f)

        glEnable(GL_TEXTURE_2D)
        glBindTexture(GL_TEXTURE_2D, canvasTexture)

        glBegin(GL_QUADS)
        glTexCoord2i(0, 0)
        glVertex2i(0, 0)
        glTexCoord2i(1, 0)
        glVertex2i(windowWidth, 0)
        glTexCoord2i(1, 1)
        glVertex2i(windowWidth, windowHeight)
        glTexCoord2i(0, 1)
        glVertex2i(0, windowHeight)
        glEnd()

        glBindTexture(GL_TEXTURE_2D, 0)
        glDisable(GL_TEXTURE_2D)
    }

    private fun renderMousePosition() {
        if (xMapMousePos == OUT_OF_BOUNDS || yMapMousePos == OUT_OF_BOUNDS || isTileItemSelectMode) {
            return
        }

        val xPos = ((xMapMousePos - 1) * iconSize + renderData.viewTranslateX) / renderData.viewScale
        val yPos = ((yMapMousePos - 1) * iconSize + renderData.viewTranslateY) / renderData.viewScale
        val realIconSize = iconSize / renderData.viewScale

        glColor4f(1f, 1f, 1f, 0.25f)

        glBegin(GL_QUADS)
        glVertex2d(xPos, yPos)
        glVertex2d(xPos + realIconSize, yPos)
        glVertex2d(xPos + realIconSize, yPos + realIconSize)
        glVertex2d(xPos, yPos + realIconSize)
        glEnd()
    }

    private fun renderMarkedPosition() {
        markedPosition?.let { pos ->
            val xPos = ((pos.x - 1) * iconSize + renderData.viewTranslateX) / renderData.viewScale
            val yPos = ((pos.y - 1) * iconSize + renderData.viewTranslateY) / renderData.viewScale
            val realIconSize = iconSize / renderData.viewScale

            glColor4f(1f, 0f, 0f, 1f)
            glLineWidth(4f)

            glBegin(GL_LINE_LOOP)
            glVertex2d(xPos, yPos)
            glVertex2d(xPos + realIconSize, yPos)
            glVertex2d(xPos + realIconSize, yPos + realIconSize)
            glVertex2d(xPos, yPos + realIconSize)
            glEnd()

            glLineWidth(1f)
        }
    }

    private fun renderSelectedTiles() {
        selectedTiles?.forEach { pos ->
            val xPos = ((pos.x - 1) * iconSize + renderData.viewTranslateX) / renderData.viewScale
            val yPos = ((pos.y - 1) * iconSize + renderData.viewTranslateY) / renderData.viewScale
            val realIconSize = iconSize / renderData.viewScale

            glColor4f(1f, 1f, 1f, 1f)

            glBegin(GL_LINE_LOOP)
            glVertex2d(xPos, yPos)
            glVertex2d(xPos + realIconSize, yPos)
            glVertex2d(xPos + realIconSize, yPos + realIconSize)
            glVertex2d(xPos, yPos + realIconSize)
            glEnd()
        }
    }

    private fun renderSelectedArea() {
        selectedArea?.let { mapArea ->
            val xPosStart = ((mapArea.x1 - 1) * iconSize + renderData.viewTranslateX) / renderData.viewScale
            val yPosStart = ((mapArea.y1 - 1) * iconSize + renderData.viewTranslateY) / renderData.viewScale

            val xPosEnd = ((mapArea.x2 - 1) * iconSize + renderData.viewTranslateX) / renderData.viewScale
            val yPosEnd = ((mapArea.y2 - 1) * iconSize + renderData.viewTranslateY) / renderData.viewScale

            val realIconSize = iconSize / renderData.viewScale

            glColor4f(1f, 1f, 1f, .25f)

            glBegin(GL_QUADS)
            glVertex2d(xPosStart, yPosStart)
            glVertex2d(xPosEnd + realIconSize, yPosStart)
            glVertex2d(xPosEnd + realIconSize, yPosEnd + realIconSize)
            glVertex2d(xPosStart, yPosEnd + realIconSize)
            glEnd()

            if (highlightSelectedArea) {
                glColor4f(0f, 1f, 0f, .75f)
                glLineWidth(2f)
                glBegin(GL_LINE_LOOP)
                glVertex2d(xPosStart, yPosStart)
                glVertex2d(xPosEnd + realIconSize, yPosStart)
                glVertex2d(xPosEnd + realIconSize, yPosEnd + realIconSize)
                glVertex2d(xPosStart, yPosEnd + realIconSize)
                glEnd()
                glLineWidth(1f)
            }
        }
    }

    // Method will render map to the separate texture which will be reused later to avoid CPU usage while idle.
    private fun fillCanvasTexture() {
        val viewWidthWithScale = windowWidth * renderData.viewScale
        val viewHeightWithScale = windowHeight * renderData.viewScale

        // Magic is happens in a separate framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer)

        if (!isTextureAttached) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, canvasTexture, 0)
            isTextureAttached = true
        }

        glViewport(0, 0, windowWidth, windowHeight)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, viewWidthWithScale, 0.0, viewHeightWithScale, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()

        glClearColor(.25f, .25f, .5f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        glEnable(GL_TEXTURE_2D)

        var currentTexture = -1

        val currentMarkedTileItemLvl = markedTileItemLvl
        markedTileItemLvl = -1

        for (frameMesh in frameMeshes) {
            val (tileItemId, sprite, x1, y1, x2, y2) = frameMesh

            var colorR = frameMesh.colorR
            var colorG = frameMesh.colorG
            var colorB = frameMesh.colorB
            var colorA = frameMesh.colorA

            val rx1 = x1 + renderData.viewTranslateX
            val ry1 = y1 + renderData.viewTranslateY
            val rx2 = x2 + renderData.viewTranslateX
            val ry2 = y2 + renderData.viewTranslateY

            if (viewWidthWithScale < rx1 || viewHeightWithScale < ry1 || rx2 < 0 || ry2 < 0) {
                continue
            }

            // More effectively would be to merge all textures into one atlas instead of such batching, but this is fine too.
            if (currentTexture != sprite.textureId) {
                if (currentTexture != -1) {
                    glEnd()
                }

                glBindTexture(GL_TEXTURE_2D, sprite.textureId)

                currentTexture = sprite.textureId
                glBegin(GL_QUADS)
            }

            // Detect tile item under the mouse and make it highlighted with the green color
            if (isTileItemSelectMode && mousePosX in rx1..rx2 && mousePosY in ry1..ry2) {
                glEnd() // We should stop render routine to read texture from the GPU properly

                if (isMouseOverTileItem(rx1, ry1, sprite)) {
                    if (currentMarkedTileItemLvl == ++markedTileItemLvl) {
                        colorR = tileItemSelectColor.x
                        colorG = tileItemSelectColor.y
                        colorB = tileItemSelectColor.z
                        colorA = tileItemSelectColor.w
                        tileItemIdMouseOver = tileItemId
                    }
                }

                glBegin(GL_QUADS)
            }

            glColor4f(colorR, colorG, colorB, colorA)

            glTexCoord2f(sprite.u2, sprite.v1)
            glVertex2d(rx2, ry2)
            glTexCoord2f(sprite.u1, sprite.v1)
            glVertex2d(rx1, ry2)
            glTexCoord2f(sprite.u1, sprite.v2)
            glVertex2d(rx1, ry1)
            glTexCoord2f(sprite.u2, sprite.v2)
            glVertex2d(rx2, ry1)
        }

        glEnd()
        glBindTexture(GL_TEXTURE_2D, 0)

        glDisable(GL_TEXTURE_2D)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private fun isMouseOverTileItem(rx1: Double, ry1: Double, sprite: IconSprite): Boolean {
        val xOffset = (mousePosX - rx1).toInt()
        val yOffset = sprite.iconHeight - (mousePosY - ry1 + .5).toInt()
        val neededPixelsBufferSize = sprite.textureHeight * sprite.textureWidth * 4

        if (neededPixelsBufferSize > pixelsBuffer.limit()) {
            pixelsBuffer = BufferUtils.createByteBuffer(neededPixelsBufferSize)
            System.gc()
        }

        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixelsBuffer)

        val positionOfAlpha = 4 * ((sprite.y1 + yOffset) * sprite.textureWidth + (sprite.x1 + xOffset)) + 3
        if (positionOfAlpha < pixelsBuffer.limit()) {
            return pixelsBuffer.get(positionOfAlpha) != 0.toByte()
        }

        return false
    }

    private fun createCanvasTexture() {
        invalidateCanvasTexture()
        canvasTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, canvasTexture)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, windowWidth, windowHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}
