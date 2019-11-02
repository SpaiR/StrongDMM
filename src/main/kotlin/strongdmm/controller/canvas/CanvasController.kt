package strongdmm.controller.canvas

import glm_.vec2.Vec2
import imgui.ImGui
import strongdmm.byond.dmm.Dmm
import strongdmm.controller.frame.FrameMesh
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.Message

class CanvasController : EventSender, EventConsumer {
    companion object {
        private const val ZOOM_FACTOR: Double = 1.5
        private const val MIN_SCALE: Int = 0
        private const val MAX_SCALE: Int = 12
    }

    private val renderDataStorage: MutableMap<String, RenderData> = mutableMapOf()
    private lateinit var renderData: RenderData

    private var isHasMap: Boolean = false

    private val canvasRenderer = CanvasRenderer()

    init {
        consumeEvent(Event.GLOBAL_SWITCH_MAP, ::handleSwitchMap)
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
        consumeEvent(Event.CANVAS_VIEW_TRANSLATE, ::handleViewTranslate)
        consumeEvent(Event.CANVAS_VIEW_SCALE, ::handleViewScale)
    }

    fun process(windowWidth: Int, windowHeight: Int) {
        if (isHasMap) {
            sendEvent<List<FrameMesh>>(Event.FRAME_COMPOSE) {
                canvasRenderer.render(it, windowWidth, windowHeight, renderData)
            }
        }
    }

    private fun handleSwitchMap(msg: Message<Dmm, Unit>) {
        renderData = renderDataStorage.getOrPut(msg.body.mapPath) { RenderData() }
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = true
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        renderDataStorage.clear()
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = false
    }

    private fun handleViewTranslate(msg: Message<Vec2, Unit>) {
        if (isHasMap) {
            val (x, y) = msg.body
            canvasRenderer.run {
                renderData.viewTranslateX += x * renderData.viewScale
                renderData.viewTranslateY -= y * renderData.viewScale
                redraw = true
            }
        }
    }

    private fun handleViewScale(msg: Message<Boolean, Unit>) {
        val isZoomIn = msg.body
        val (x, y) = ImGui.mousePos

        // When the editor isn't in the focus
        if (x < 0 || y < 0) {
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
