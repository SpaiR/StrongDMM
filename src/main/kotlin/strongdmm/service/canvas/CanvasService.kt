package strongdmm.service.canvas

import gnu.trove.map.hash.TIntObjectHashMap
import imgui.ImBool
import imgui.ImGui
import imgui.ImVec2
import imgui.enums.ImGuiHoveredFlags
import imgui.enums.ImGuiMouseButton
import org.lwjgl.glfw.GLFW
import strongdmm.PostInitialize
import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_ICON_SIZE
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.*
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.*
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.event.type.ui.TriggerTilePopupUi
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.frame.FrameMesh
import strongdmm.service.frame.FramedTile
import strongdmm.util.DEFAULT_ICON_SIZE
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.extension.getOrPut
import strongdmm.util.imgui.GREEN_RGBA
import strongdmm.util.imgui.RED_RGBA
import strongdmm.window.AppWindow
import java.util.*

class CanvasService : EventHandler, PostInitialize {
    companion object {
        private const val ZOOM_FACTOR: Double = 1.5
        private const val MIN_SCALE: Int = 0
        private const val MAX_SCALE: Int = 12
    }

    private val renderDataStorageByMapId: TIntObjectHashMap<RenderData> = TIntObjectHashMap()
    private lateinit var renderData: RenderData

    private var selectedTileItem: TileItem? = null

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
    private var isMovingCanvasWithLmb: Boolean = false
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
        consumeEvent(Reaction.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        consumeEvent(Reaction.TilePopupOpened::class.java, ::handleTilePopupOpened)
        consumeEvent(Reaction.TilePopupClosed::class.java, ::handleTilePopupClosed)
        consumeEvent(Provider.FrameControllerComposedFrame::class.java, ::handleProviderFrameControllerComposedFrame)
        consumeEvent(Provider.FrameControllerFramedTiles::class.java, ::handleProviderFrameControllerFramedTiles)
        consumeEvent(TriggerCanvasService.CenterCanvasByPosition::class.java, ::handleCenterCanvasByPosition)
        consumeEvent(TriggerCanvasService.MarkPosition::class.java, ::handleMarkPosition)
        consumeEvent(TriggerCanvasService.ResetMarkedPosition::class.java, ::handleResetMarkedPosition)
        consumeEvent(TriggerCanvasService.SelectTiles::class.java, ::handleSelectTiles)
        consumeEvent(TriggerCanvasService.ResetSelectedTiles::class.java, ::handleResetSelectedTiles)
        consumeEvent(TriggerCanvasService.SelectArea::class.java, ::handleSelectArea)
        consumeEvent(TriggerCanvasService.ResetSelectedArea::class.java, ::handleResetSelectedArea)
    }

    override fun postInit() {
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

        if (ImGui.isKeyDown(GLFW.GLFW_KEY_SPACE) && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            isMovingCanvasWithLmb = true
        }

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

        sendEvent(TriggerMapHolderService.FetchSelectedMap {
            if (xMapMousePos != OUT_OF_BOUNDS && yMapMousePos != OUT_OF_BOUNDS) {
                sendEvent(TriggerTilePopupUi.Open(it.getTile(xMapMousePos, yMapMousePos, it.zSelected)))
            }
        })
    }

    private fun processMapMouseDrag() {
        if (isMovingCanvasWithLmb || isBlockCanvasInteraction || ImGui.getIO().keyShift) {
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
                    sendEvent(TriggerMapHolderService.FetchSelectedMap { currentMap ->
                        deleteTileItemUnderMouse(currentMap)
                    })
                } else { // Select tile item
                    sendEvent(TriggerTileItemService.ChangeSelectedTileItem(GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)))
                }
            } else if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                sendEvent(TriggerMapHolderService.FetchSelectedMap { currentMap ->
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

    private fun replaceTileItemUnderMouseWithSelected(map: Dmm) {
        if (selectedTileItem == null) {
            return
        }

        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val x = canvasRenderer.xForTileItemMouseOver
        val y = canvasRenderer.yForTileItemMouseOver
        val tile = map.getTile(x, y, map.zSelected)

        sendEvent(
            TriggerActionService.AddAction(
                ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem, selectedTileItem!!)
                }
            )
        )

        sendEvent(TriggerFrameService.RefreshFrame())
    }

    private fun deleteTileItemUnderMouse(map: Dmm) {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val x = canvasRenderer.xForTileItemMouseOver
        val y = canvasRenderer.yForTileItemMouseOver
        val tile = map.getTile(x, y, map.zSelected)

        sendEvent(
            TriggerActionService.AddAction(
                ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem)
                }
            )
        )

        sendEvent(TriggerFrameService.RefreshFrame())
    }

    private fun openTileItemUnderMouseForEdit(map: Dmm) {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val x = canvasRenderer.xForTileItemMouseOver
        val y = canvasRenderer.yForTileItemMouseOver
        val tile = map.getTile(x, y, map.zSelected)
        val tileItemIdx = tile.getTileItemIdx(tileItem)

        sendEvent(TriggerEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
    }

    private fun postProcessCanvasMovingChecks() {
        if (isMovingCanvasWithLmb && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            isMovingCanvasWithLmb = false
        }
    }

    private fun isImGuiInUse(): Boolean {
        return ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow or ImGuiHoveredFlags.AllowWhenBlockedByPopup or ImGuiHoveredFlags.AllowWhenBlockedByActiveItem) ||
            ImGui.isAnyItemHovered() || ImGui.isAnyItemActive()
    }

    private fun handleApplicationBlockChanged(event: Event<Boolean, Unit>) {
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

    private fun handleSelectedTileItemChanged(event: Event<TileItem?, Unit>) {
        selectedTileItem = event.body
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
