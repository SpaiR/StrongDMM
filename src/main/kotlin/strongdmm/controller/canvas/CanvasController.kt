package strongdmm.controller.canvas

import gnu.trove.map.hash.TIntObjectHashMap
import imgui.ImBool
import imgui.ImGui
import imgui.ImVec2
import imgui.enums.ImGuiHoveredFlags
import imgui.enums.ImGuiMouseButton
import org.lwjgl.glfw.GLFW
import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_ICON_SIZE
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.*
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.frame.FrameMesh
import strongdmm.controller.frame.FramedTile
import strongdmm.event.ApplicationBlockStatus
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.*
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.event.type.ui.TriggerTilePopupUi
import strongdmm.util.DEFAULT_ICON_SIZE
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.extension.getOrPut
import strongdmm.util.imgui.GREEN_RGBA
import strongdmm.util.imgui.RED_RGBA
import strongdmm.window.AppWindow
import java.util.Timer
import java.util.TimerTask

class CanvasController : EventSender, EventConsumer {
    companion object {
        private const val ZOOM_FACTOR: Double = 1.5
        private const val MIN_SCALE: Int = 0
        private const val MAX_SCALE: Int = 12
    }

    private val renderDataStorageByMapId: TIntObjectHashMap<RenderData> = TIntObjectHashMap()
    private lateinit var renderData: RenderData

    private var activeTileItem: TileItem? = null

    private var isCanvasBlocked: Boolean = false

    private var isHasMap: Boolean = false
    private var iconSize: Int = DEFAULT_ICON_SIZE

    private var maxX: Int = OUT_OF_BOUNDS
    private var maxY: Int = OUT_OF_BOUNDS

    private val frameAreas: ImBool = ImBool(true)

    // Tile of the map covered with mouse
    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    private var isMapMouseDragged: Boolean = false
    private var isMovingCanvas: Boolean = false
    private var isBlockCanvasInteraction: Boolean = false

    // To handle user input
    private val mousePos: ImVec2 = ImVec2()
    private val mouseDelta: ImVec2 = ImVec2()

    private val canvasRenderer = CanvasRenderer()

    init {
        consumeEvent(Reaction.ApplicationBlockChanged::class.java, ::handleApplicationBlockChanged)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapMapSizeChanged::class.java, ::handleSelectedMapMapSizeChanged)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(Reaction.FrameRefreshed::class.java, ::handleFrameRefreshed)
        consumeEvent(Reaction.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(Reaction.TilePopupOpened::class.java, ::handleTilePopupOpened)
        consumeEvent(Reaction.TilePopupClosed::class.java, ::handleTilePopupClosed)
        consumeEvent(Provider.FrameControllerComposedFrame::class.java, ::handleProviderFrameControllerComposedFrame)
        consumeEvent(Provider.FrameControllerFramedTiles::class.java, ::handleProviderFrameControllerFramedTiles)
        consumeEvent(TriggerCanvasController.CenterCanvasByPosition::class.java, ::handleCenterCanvasByPosition)
        consumeEvent(TriggerCanvasController.MarkPosition::class.java, ::handleMarkPosition)
        consumeEvent(TriggerCanvasController.ResetMarkedPosition::class.java, ::handleResetMarkedPosition)
        consumeEvent(TriggerCanvasController.SelectTiles::class.java, ::handleSelectTiles)
        consumeEvent(TriggerCanvasController.ResetSelectedTiles::class.java, ::handleResetSelectedTiles)
        consumeEvent(TriggerCanvasController.SelectArea::class.java, ::handleSelectArea)
        consumeEvent(TriggerCanvasController.ResetSelectedArea::class.java, ::handleResetSelectedArea)
    }

    fun postInit() {
        sendEvent(Provider.CanvasControllerFrameAreas(frameAreas))
    }

    fun process() {
        if (!isHasMap) {
            return
        }

        ImGui.getMousePos(mousePos)

        if (!isCanvasBlocked && !isImGuiInUse()) {
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
        postProcessCanvasMovingChecks()
    }

    private fun processViewTranslate() {
        if (!(ImGui.isMouseDown(ImGuiMouseButton.Middle) || (ImGui.isKeyDown(GLFW.GLFW_KEY_SPACE) && ImGui.isMouseDown(ImGuiMouseButton.Left)))) {
            return
        }

        isMovingCanvas = true
        ImGui.getIO().getMouseDelta(mouseDelta)

        if (mouseDelta.x != 0f || mouseDelta.y != 0f) {
            canvasRenderer.run {
                renderData.viewTranslateX += mouseDelta.x * renderData.viewScale
                renderData.viewTranslateY -= mouseDelta.y * renderData.viewScale
                redraw = true
            }

            sendEvent(TriggerTilePopupUi.Close())
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

        sendEvent(TriggerTilePopupUi.Close())
    }

    private fun processTilePopupClick() {
        if (!ImGui.isMouseClicked(ImGuiMouseButton.Right) || ImGui.getIO().keyShift) {
            return
        }

        sendEvent(TriggerMapHolderController.FetchSelectedMap {
            if (xMapMousePos != OUT_OF_BOUNDS && yMapMousePos != OUT_OF_BOUNDS) {
                sendEvent(TriggerTilePopupUi.Open(it.getTile(xMapMousePos, yMapMousePos, it.zActive)))
            }
        })
    }

    private fun processMapMouseDrag() {
        if (isMovingCanvas || isBlockCanvasInteraction || ImGui.getIO().keyShift) {
            return
        }

        if (ImGui.isMouseDown(ImGuiMouseButton.Left) && !isMapMouseDragged) {
            isMapMouseDragged = true
            sendEvent(Reaction.MapMouseDragStarted())
        } else if (!ImGui.isMouseDown(ImGuiMouseButton.Left) && isMapMouseDragged) {
            isMapMouseDragged = false
            sendEvent(Reaction.MapMouseDragStopped())
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
            sendEvent(Reaction.MapMousePosChanged(MapPos(xMapMousePos, yMapMousePos)))
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
        canvasRenderer.realIconSize = (iconSize / renderData.viewScale).toInt()
        canvasRenderer.mousePosX = mousePos.x * renderData.viewScale.toFloat()
        canvasRenderer.mousePosY = (AppWindow.windowHeight - mousePos.y) * renderData.viewScale.toFloat()
        canvasRenderer.frameAreas = frameAreas.get()
    }

    private fun postProcessTileItemSelectMode() {
        if (!canvasRenderer.isTileItemSelectMode) {
            return
        }

        if (canvasRenderer.tileItemIdMouseOver != 0L) {
            if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
                if (ImGui.getIO().keyCtrl) { // Delete tile item
                    sendEvent(TriggerMapHolderController.FetchSelectedMap { currentMap ->
                        deleteTileItemUnderMouse(currentMap)
                    })
                } else { // Select tile item
                    sendEvent(TriggerTileItemController.ChangeActiveTileItem(GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)))
                }
            } else if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                sendEvent(TriggerMapHolderController.FetchSelectedMap { currentMap ->
                    if (ImGui.getIO().keyCtrl) { // Replace tile item
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
        if (tileItem.stepX != 0) {
            x += (-1 * tileItem.stepX / iconSize.toFloat() + if (tileItem.stepX > 0) -.5 else .5).toInt()
        }
        if (tileItem.pixelY != 0) {
            y += (-1 * tileItem.pixelY / iconSize.toFloat() + if (tileItem.pixelY > 0) -.5 else .5).toInt()
        }
        if (tileItem.stepY != 0) {
            y += (-1 * tileItem.stepY / iconSize.toFloat() + if (tileItem.stepY > 0) -.5 else .5).toInt()
        }

        return MapPos(x, y)
    }

    private fun replaceTileItemUnderMouseWithSelected(map: Dmm) {
        if (activeTileItem == null) {
            return
        }

        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val pos = getTileItemCoordUnderMouse()
        val tile = map.getTile(pos.x, pos.y, map.zActive)

        sendEvent(
            TriggerActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem, activeTileItem!!)
                }
            )
        )

        sendEvent(TriggerFrameController.RefreshFrame())
    }

    private fun deleteTileItemUnderMouse(map: Dmm) {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val pos = getTileItemCoordUnderMouse()
        val tile = map.getTile(pos.x, pos.y, map.zActive)

        sendEvent(
            TriggerActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem)
                }
            )
        )

        sendEvent(TriggerFrameController.RefreshFrame())
    }

    private fun openTileItemUnderMouseForEdit(map: Dmm) {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val pos = getTileItemCoordUnderMouse()
        val tile = map.getTile(pos.x, pos.y, map.zActive)
        val tileItemIdx = tile.getTileItemIdx(tileItem)

        sendEvent(TriggerEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
    }

    private fun postProcessCanvasMovingChecks() {
        if (isMovingCanvas && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            isMovingCanvas = false
        }
    }

    private fun isImGuiInUse(): Boolean {
        return ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow or ImGuiHoveredFlags.AllowWhenBlockedByPopup or ImGuiHoveredFlags.AllowWhenBlockedByActiveItem) ||
            ImGui.isAnyItemHovered() || ImGui.isAnyItemActive()
    }

    private fun handleApplicationBlockChanged(event: Event<ApplicationBlockStatus, Unit>) {
        isCanvasBlocked = event.body
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        canvasRenderer.markedPosition = null
        renderData = renderDataStorageByMapId.getOrPut(event.body.id) { RenderData(event.body.id) }
        maxX = event.body.maxX
        maxY = event.body.maxY
        canvasRenderer.invalidateCanvasTexture()
        isHasMap = true
    }

    private fun handleSelectedMapMapSizeChanged(event: Event<MapSize, Unit>) {
        maxX = event.body.maxX
        maxY = event.body.maxY
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

    private fun handleTilePopupOpened() {
        isBlockCanvasInteraction = true
    }

    private fun handleTilePopupClosed() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                isBlockCanvasInteraction = false
            }
        }, 500)
    }

    private fun handleProviderFrameControllerComposedFrame(event: Event<List<FrameMesh>, Unit>) {
        canvasRenderer.providedFrameMeshes = event.body
    }

    private fun handleProviderFrameControllerFramedTiles(event: Event<List<FramedTile>, Unit>) {
        canvasRenderer.providedFramedTiles = event.body
    }

    private fun handleCenterCanvasByPosition(event: Event<MapPos, Unit>) {
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
    }
}
