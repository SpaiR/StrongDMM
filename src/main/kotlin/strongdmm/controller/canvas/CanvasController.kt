package strongdmm.controller.canvas

import glm_.vec2.Vec2i
import imgui.HoveredFlag
import imgui.ImGui
import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_ICON_SIZE
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapId
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.DEFAULT_ICON_SIZE
import strongdmm.util.LMB
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.RMB

class CanvasController : EventSender, EventConsumer {
    companion object {
        private const val ZOOM_FACTOR: Double = 1.5
        private const val MIN_SCALE: Int = 0
        private const val MAX_SCALE: Int = 12
    }

    private val renderDataStorage: MutableMap<MapId, RenderData> = mutableMapOf()
    private lateinit var renderData: RenderData

    private var modalBlock: Boolean = false
    private var isHasMap: Boolean = false
    private var iconSize: Int = DEFAULT_ICON_SIZE

    private var maxX: Int = OUT_OF_BOUNDS
    private var maxY: Int = OUT_OF_BOUNDS

    // Tile of the map covered with mouse
    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    private val canvasRenderer = CanvasRenderer()

    init {
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.SwitchEnvironment::class.java, ::handleSwitchEnvironment)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
        consumeEvent(Event.Global.RefreshFrame::class.java, ::handleRefreshFrame)
        consumeEvent(Event.Global.ModalBlock::class.java, ::handleModalBlock)
    }

    fun process(windowWidth: Int, windowHeight: Int) {
        if (isHasMap) {
            if (!modalBlock && !isImGuiInUse()) {
                processViewTranslate()
                processViewScale()
                processTilePopupClick()
                calculateMapMousePos(windowHeight)
            }

            sendEvent(Event.FrameController.Compose {
                canvasRenderer.render(it, windowWidth, windowHeight, renderData, xMapMousePos, yMapMousePos, iconSize)
            })
        }
    }

    private fun processViewTranslate() {
        if (!ImGui.isMouseDown(LMB)) {
            return
        }

        if (ImGui.io.mouseDelta anyNotEqual 0f) {
            val (x, y) = ImGui.io.mouseDelta
            canvasRenderer.run {
                renderData.viewTranslateX += x * renderData.viewScale
                renderData.viewTranslateY -= y * renderData.viewScale
                redraw = true
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

        sendEvent(Event.TilePopupUi.Close())
    }

    private fun processTilePopupClick() {
        if (ImGui.isMouseClicked(RMB)) {
            sendEvent(Event.MapController.FetchSelected {
                if (it != null && xMapMousePos != OUT_OF_BOUNDS && yMapMousePos != OUT_OF_BOUNDS) {
                    sendEvent(Event.TilePopupUi.Open(it.getTile(xMapMousePos, yMapMousePos)))
                }
            })
        }
    }

    private fun calculateMapMousePos(windowHeight: Int) {
        val (x, y) = ImGui.mousePos

        val xMap = (x * renderData.viewScale - renderData.viewTranslateX) / iconSize
        val yMap = ((windowHeight - y) * renderData.viewScale - renderData.viewTranslateY) / iconSize

        val xMapMousePosNew = if (xMap > 0 && xMap <= maxX) xMap.toInt() + 1 else OUT_OF_BOUNDS
        val yMapMousePosNew = if (yMap > 0 && yMap <= maxY) yMap.toInt() + 1 else OUT_OF_BOUNDS

        if (xMapMousePos != xMapMousePosNew || yMapMousePos != yMapMousePosNew) {
            xMapMousePos = xMapMousePosNew
            yMapMousePos = yMapMousePosNew
            sendEvent(Event.Global.UpdMapMousePos(Vec2i(xMapMousePos, yMapMousePos)))
        }
    }

    private fun isImGuiInUse(): Boolean = ImGui.isWindowHovered(HoveredFlag.AnyWindow) || ImGui.isAnyItemHovered || ImGui.isAnyItemActive

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        renderData = renderDataStorage.getOrPut(event.body.id) { RenderData() }
        maxX = event.body.maxX
        maxY = event.body.maxY
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = true
    }

    private fun handleSwitchEnvironment(event: Event<Dme, Unit>) {
        iconSize = event.body.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: DEFAULT_ICON_SIZE
    }

    private fun handleResetEnvironment() {
        renderDataStorage.clear()
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = false
    }

    private fun handleCloseMap(event: Event<Dmm, Unit>) {
        isHasMap = renderDataStorage.remove(event.body.id) !== renderData
    }

    private fun handleRefreshFrame() {
        canvasRenderer.redraw = true
    }

    private fun handleModalBlock(event: Event<Boolean, Unit>) {
        modalBlock = event.body
    }
}
