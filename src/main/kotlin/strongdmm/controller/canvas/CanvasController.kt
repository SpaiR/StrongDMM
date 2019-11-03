package strongdmm.controller.canvas

import imgui.ImGui
import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_ICON_SIZE
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.controller.frame.FrameMesh
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.Message
import strongdmm.util.DEFAULT_ICON_SIZE
import strongdmm.util.LMB
import strongdmm.util.OUT_OF_BOUNDS

class CanvasController : EventSender, EventConsumer {
    companion object {
        private const val ZOOM_FACTOR: Double = 1.5
        private const val MIN_SCALE: Int = 0
        private const val MAX_SCALE: Int = 12
    }

    private val renderDataStorage: MutableMap<String, RenderData> = mutableMapOf()
    private lateinit var renderData: RenderData

    private var isHasMap: Boolean = false
    private var iconSize: Int = DEFAULT_ICON_SIZE

    private var maxX: Int = OUT_OF_BOUNDS
    private var maxY: Int = OUT_OF_BOUNDS

    // Tile of the map covered with mouse
    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    private val canvasRenderer = CanvasRenderer()

    init {
        consumeEvent(Event.GLOBAL_SWITCH_MAP, ::handleSwitchMap)
        consumeEvent(Event.GLOBAL_SWITCH_ENVIRONMENT, ::handleSwitchEnvironment)
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
    }

    fun process(windowWidth: Int, windowHeight: Int) {
        if (isHasMap) {
            processViewTranslate()
            processViewScale()

            calculateMapMousePos(windowHeight)

            sendEvent<List<FrameMesh>>(Event.FRAME_COMPOSE) {
                canvasRenderer.render(it, windowWidth, windowHeight, renderData, xMapMousePos, yMapMousePos, iconSize)
            }
        }
    }

    private fun calculateMapMousePos(windowHeight: Int) {
        val (x, y) = ImGui.mousePos

        val xMap = (x * renderData.viewScale - renderData.viewTranslateX) / iconSize
        val yMap = ((windowHeight - y) * renderData.viewScale - renderData.viewTranslateY) / iconSize

        val xMapMousePosNew = if (xMap >= 0 && xMap <= (maxX - 1)) xMap.toInt() + 1 else OUT_OF_BOUNDS
        val yMapMousePosNew = if (yMap >= 0 && yMap <= (maxY - 1)) yMap.toInt() + 1 else OUT_OF_BOUNDS

        if (xMapMousePos != xMapMousePosNew || yMapMousePos != yMapMousePosNew) {
            xMapMousePos = xMapMousePosNew
            yMapMousePos = yMapMousePosNew
        }
    }

    private fun handleSwitchMap(msg: Message<Dmm, Unit>) {
        renderData = renderDataStorage.getOrPut(msg.body.mapPath) { RenderData() }
        maxX = msg.body.getMaxX()
        maxY = msg.body.getMaxY()
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = true
    }

    private fun handleSwitchEnvironment(msg: Message<Dme, Unit>) {
        iconSize = msg.body.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: DEFAULT_ICON_SIZE
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        renderDataStorage.clear()
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = false
    }

    private fun processViewTranslate() {
        if (ImGui.isMouseDown(LMB)) {
            if (ImGui.io.mouseDelta anyNotEqual 0f) {
                val (x, y) = ImGui.io.mouseDelta
                canvasRenderer.run {
                    renderData.viewTranslateX += x * renderData.viewScale
                    renderData.viewTranslateY -= y * renderData.viewScale
                    redraw = true
                }
            }
        }
    }

    private fun processViewScale() {
        if (ImGui.io.mouseWheel == 0f) {
            return
        }

        val isZoomIn = ImGui.io.mouseWheel > 0
        val (x, y) = ImGui.mousePos

        if (!isHasMap || x < 0 || y < 0) {
            return
        }

        if ((isZoomIn && renderData.scaleCount - 1 < MIN_SCALE) || (!isZoomIn && renderData.scaleCount + 1 > MAX_SCALE)) {
            return
        } else {
            renderData.scaleCount += if (isZoomIn) -1 else 1
        }

        canvasRenderer.run {
            if (isZoomIn) {
                renderData.viewScale /= ZOOM_FACTOR
                renderData.viewTranslateX -= x * renderData.viewScale / 2
                renderData.viewTranslateY -= (windowHeight - y) * renderData.viewScale / 2
            } else {
                renderData.viewTranslateX += x * renderData.viewScale / 2
                renderData.viewTranslateY += (windowHeight - y) * renderData.viewScale / 2
                renderData.viewScale *= ZOOM_FACTOR
            }

            redraw = true
        }
    }
}
