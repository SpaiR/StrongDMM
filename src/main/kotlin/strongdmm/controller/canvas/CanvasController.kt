package strongdmm.controller.canvas

import gnu.trove.map.hash.TIntObjectHashMap
import imgui.ImGui
import imgui.ImVec2
import imgui.enums.ImGuiHoveredFlags
import imgui.enums.ImGuiMouseButton
import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_ICON_SIZE
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.*
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.frame.FrameMesh
import strongdmm.event.CanvasBlockStatus
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventFrameController
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.EventTileItemController
import strongdmm.util.DEFAULT_ICON_SIZE
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.extension.getOrPut
import strongdmm.util.imgui.GREEN_RGBA
import strongdmm.util.imgui.RED_RGBA
import strongdmm.window.AppWindow

class CanvasController : EventSender, EventConsumer {
    companion object {
        private const val ZOOM_FACTOR: Double = 1.5
        private const val MIN_SCALE: Int = 0
        private const val MAX_SCALE: Int = 12
    }

    private val renderDataStorageByMapId: TIntObjectHashMap<RenderData> = TIntObjectHashMap()
    private lateinit var renderData: RenderData

    private var activeTileItem: TileItem? = null

    private var isBlocked: Boolean = false
    private var isHasMap: Boolean = false
    private var iconSize: Int = DEFAULT_ICON_SIZE

    private var maxX: Int = OUT_OF_BOUNDS
    private var maxY: Int = OUT_OF_BOUNDS

    // Tile of the map covered with mouse
    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    private var isMapMouseDragged: Boolean = false

    // To handle user input
    private val mousePos: ImVec2 = ImVec2()
    private val mouseDelta: ImVec2 = ImVec2()

    private val canvasRenderer = CanvasRenderer()

    init {
        consumeEvent(EventGlobal.OpenedMapChanged::class.java, ::handleOpenedMapChanged)
        consumeEvent(EventGlobal.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(EventGlobal.FrameRefreshed::class.java, ::handleFrameRefreshed)
        consumeEvent(EventGlobal.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(EventGlobalProvider.ComposedFrame::class.java, ::handleProviderComposedFrame)
        consumeEvent(Event.CanvasController.Block::class.java, ::handleCanvasBlock)
        consumeEvent(Event.CanvasController.CenterPosition::class.java, ::handleCenterPosition)
        consumeEvent(Event.CanvasController.MarkPosition::class.java, ::handleMarkPosition)
        consumeEvent(Event.CanvasController.ResetMarkedPosition::class.java, ::handleResetMarkedPosition)
        consumeEvent(Event.CanvasController.SelectTiles::class.java, ::handleSelectTiles)
        consumeEvent(Event.CanvasController.ResetSelectedTiles::class.java, ::handleResetSelectedTiles)
        consumeEvent(Event.CanvasController.SelectArea::class.java, ::handleSelectArea)
        consumeEvent(Event.CanvasController.ResetSelectedArea::class.java, ::handleResetSelectedArea)
        consumeEvent(Event.CanvasController.HighlightSelectedArea::class.java, ::handleHighlightSelectedArea)
    }

    fun process() {
        if (!isHasMap) {
            return
        }

        ImGui.getMousePos(mousePos)

        if (!isBlocked && !isImGuiInUse()) {
            processViewTranslate()
            processViewScale()
            processTilePopupClick()
            processMapMouseDrag()
            processMapMousePosition()
            processTileItemSelectMode()
        }

        prepareCanvasRenderer()
        canvasRenderer.render()

        postProcessTileItemSelectMode()
    }

    private fun processViewTranslate() {
        if (!(ImGui.isMouseDown(ImGuiMouseButton.Middle) || (ImGui.getIO().keyAlt && ImGui.isMouseDown(ImGuiMouseButton.Left)))) {
            return
        }

        ImGui.getIO().getMouseDelta(mouseDelta)

        if (mouseDelta.x != 0f || mouseDelta.y != 0f) {
            canvasRenderer.run {
                renderData.viewTranslateX += mouseDelta.x * renderData.viewScale
                renderData.viewTranslateY -= mouseDelta.y * renderData.viewScale
                redraw = true
            }

            sendEvent(Event.TilePopupUi.Close())
        }
    }

    private fun processViewScale() {
        val mouseWheel = ImGui.getIO().mouseWheel

        if (mouseWheel == 0f) {
            return
        }

        val isZoomIn = mouseWheel > 0
        val x = mousePos.x
        val y = mousePos.y

        if (!isHasMap || x < 0 || y < 0) {
            return
        }

        // I guess it could be simplified, but it works as a scale limiter
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
        if (!ImGui.isMouseClicked(ImGuiMouseButton.Right) || ImGui.getIO().keyShift) {
            return
        }

        sendEvent(Event.MapHolderController.FetchSelected {
            if (xMapMousePos != OUT_OF_BOUNDS && yMapMousePos != OUT_OF_BOUNDS) {
                sendEvent(Event.TilePopupUi.Open(it.getTile(xMapMousePos, yMapMousePos)))
            }
        })
    }

    private fun processMapMouseDrag() {
        if (ImGui.getIO().keyShift) {
            return // do not do anything while this modifier is in play, since it's used by SHIFT+Click actions
        }

        if (ImGui.isMouseDown(ImGuiMouseButton.Left) && !isMapMouseDragged) {
            isMapMouseDragged = true
            sendEvent(EventGlobal.MapMouseDragStarted())
        } else if (!ImGui.isMouseDown(ImGuiMouseButton.Left) && isMapMouseDragged) {
            isMapMouseDragged = false
            sendEvent(EventGlobal.MapMouseDragStopped())
        }
    }

    private fun processMapMousePosition() {
        val x = mousePos.x
        val y = mousePos.y

        val xMap = (x * renderData.viewScale - renderData.viewTranslateX) / iconSize
        val yMap = ((AppWindow.windowHeight - y) * renderData.viewScale - renderData.viewTranslateY) / iconSize

        val xMapMousePosNew = if (xMap > 0 && xMap <= maxX) xMap.toInt() + 1 else OUT_OF_BOUNDS
        val yMapMousePosNew = if (yMap > 0 && yMap <= maxY) yMap.toInt() + 1 else OUT_OF_BOUNDS

        if (xMapMousePos != xMapMousePosNew || yMapMousePos != yMapMousePosNew) {
            xMapMousePos = xMapMousePosNew
            yMapMousePos = yMapMousePosNew
            sendEvent(EventGlobal.MapMousePosChanged(MapPos(xMapMousePos, yMapMousePos)))
        }
    }

    private fun processTileItemSelectMode() {
        if (ImGui.getIO().keyShift) {
            canvasRenderer.isTileItemSelectMode = true
            canvasRenderer.tileItemSelectColor = if (ImGui.getIO().keyCtrl) RED_RGBA else GREEN_RGBA
        }
    }

    private fun prepareCanvasRenderer() {
        canvasRenderer.renderData = renderData
        canvasRenderer.xMapMousePos = xMapMousePos
        canvasRenderer.yMapMousePos = yMapMousePos
        canvasRenderer.iconSize = iconSize
        canvasRenderer.mousePosX = mousePos.x * renderData.viewScale.toFloat()
        canvasRenderer.mousePosY = (AppWindow.windowHeight - mousePos.y) * renderData.viewScale.toFloat()
    }

    private fun postProcessTileItemSelectMode() {
        if (!canvasRenderer.isTileItemSelectMode) {
            return
        }

        if (canvasRenderer.tileItemIdMouseOver != 0L) {
            if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
                if (ImGui.getIO().keyCtrl) { // Replace tile item
                    sendEvent(Event.MapHolderController.FetchSelected { currentMap ->
                        deleteTileItemUnderMouse(currentMap)
                    })
                } else { // Select tile item
                    sendEvent(EventTileItemController.ChangeActive(GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)))
                }
            } else if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                sendEvent(Event.MapHolderController.FetchSelected { currentMap ->
                    if (ImGui.getIO().keyCtrl) { // Delete tile item
                        replaceTileItemUnderMouseWithSelected(currentMap)
                    } else { // Open for edit
                        openTileItemUnderMouseForEdit(currentMap)
                    }
                })
            }
        }

        canvasRenderer.isTileItemSelectMode = false
        canvasRenderer.tileItemIdMouseOver = 0
        canvasRenderer.redraw = true // Do one more redraw, so our canvas texture will render proper data (no highlighting etc)
    }

    private fun getTileItemCoordUnderMouse(): MapPos {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)

        var x = xMapMousePos
        var y = yMapMousePos

        if (tileItem.pixelX != 0) {
            x += (-1 * tileItem.pixelX / iconSize.toFloat() + if (tileItem.pixelX > 0) -.5 else .5).toInt()
        }
        if (tileItem.pixelY != 0) {
            y += (-1 * tileItem.pixelY / iconSize.toFloat() + if (tileItem.pixelY > 0) -.5 else .5).toInt()
        }

        return MapPos(x, y)
    }

    private fun replaceTileItemUnderMouseWithSelected(map: Dmm) {
        if (activeTileItem == null) {
            return
        }

        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val pos = getTileItemCoordUnderMouse()
        val tile = map.getTile(pos.x, pos.y)

        sendEvent(Event.ActionController.AddAction(
            ReplaceTileAction(tile) {
                tile.replaceTileItem(tileItem, activeTileItem!!)
            }
        ))

        sendEvent(EventFrameController.Refresh())
    }

    private fun deleteTileItemUnderMouse(map: Dmm) {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val pos = getTileItemCoordUnderMouse()
        val tile = map.getTile(pos.x, pos.y)

        sendEvent(Event.ActionController.AddAction(
            ReplaceTileAction(tile) {
                tile.deleteTileItem(tileItem)
            }
        ))

        sendEvent(EventFrameController.Refresh())
    }

    private fun openTileItemUnderMouseForEdit(map: Dmm) {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val pos = getTileItemCoordUnderMouse()
        val tile = map.getTile(pos.x, pos.y)
        val tileItemIdx = tile.getTileItemIdx(tileItem)

        sendEvent(Event.EditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
    }

    private fun isImGuiInUse(): Boolean {
        return ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow or ImGuiHoveredFlags.AllowWhenBlockedByPopup or ImGuiHoveredFlags.AllowWhenBlockedByActiveItem) ||
            ImGui.isAnyItemHovered() || ImGui.isAnyItemActive()
    }

    private fun handleOpenedMapChanged(event: Event<Dmm, Unit>) {
        canvasRenderer.markedPosition = null
        renderData = renderDataStorageByMapId.getOrPut(event.body.id) { RenderData(event.body.id) }
        maxX = event.body.maxX
        maxY = event.body.maxY
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = true
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        iconSize = event.body.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: DEFAULT_ICON_SIZE
    }

    private fun handleEnvironmentReset() {
        canvasRenderer.markedPosition = null
        renderDataStorageByMapId.clear()
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = false
    }

    private fun handleOpenedMapClosed(event: Event<Dmm, Unit>) {
        if (renderData.mapId == event.body.id) {
            canvasRenderer.markedPosition = null
        }

        isHasMap = renderDataStorageByMapId.remove(event.body.id) !== renderData
    }

    private fun handleFrameRefreshed() {
        canvasRenderer.redraw = true
    }

    private fun handleActiveTileItemChanged(event: Event<TileItem?, Unit>) {
        activeTileItem = event.body
    }

    private fun handleProviderComposedFrame(event: Event<List<FrameMesh>, Unit>) {
        canvasRenderer.frameMeshes = event.body
    }

    private fun handleCanvasBlock(event: Event<CanvasBlockStatus, Unit>) {
        isBlocked = event.body
    }

    private fun handleCenterPosition(event: Event<MapPos, Unit>) {
        renderData.viewTranslateX = AppWindow.windowWidth / 2 * renderData.viewScale + (event.body.x - 1) * iconSize * -1.0
        renderData.viewTranslateY = AppWindow.windowHeight / 2 * renderData.viewScale + (event.body.y - 1) * iconSize * -1.0
        canvasRenderer.redraw = true
    }

    private fun handleMarkPosition(event: Event<MapPos, Unit>) {
        canvasRenderer.run {
            markedPosition = event.body
            redraw = true
        }
    }

    private fun handleResetMarkedPosition() {
        canvasRenderer.markedPosition = null
    }

    private fun handleSelectTiles(event: Event<Collection<MapPos>, Unit>) {
        canvasRenderer.selectedTiles = event.body
    }

    private fun handleResetSelectedTiles() {
        canvasRenderer.selectedTiles = null
    }

    private fun handleSelectArea(event: Event<MapArea, Unit>) {
        canvasRenderer.selectedArea = event.body
    }

    private fun handleResetSelectedArea() {
        canvasRenderer.selectedArea = null
        canvasRenderer.highlightSelectedArea = false
    }

    private fun handleHighlightSelectedArea() {
        canvasRenderer.highlightSelectedArea = true
    }
}
