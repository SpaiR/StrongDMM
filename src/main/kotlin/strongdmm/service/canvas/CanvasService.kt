package strongdmm.service.canvas

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiHoveredFlags
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiMouseCursor
import imgui.type.ImBoolean
import org.lwjgl.glfw.GLFW
import strongdmm.application.PostInitialize
import strongdmm.application.Processable
import strongdmm.application.Service
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.*
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.*
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.event.type.ui.TriggerTilePopupUi
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.frame.FrameMesh
import strongdmm.service.frame.FramedTile
import strongdmm.service.preferences.Preferences
import strongdmm.service.shortcut.Shortcut
import strongdmm.service.shortcut.ShortcutHandler
import strongdmm.util.DEFAULT_ICON_SIZE
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.application.window.Window
import strongdmm.event.type.ui.ReactionTilePopupUi
import java.util.*

class CanvasService : Service, PostInitialize, Processable {
    companion object {
        private const val ZOOM_FACTOR: Double = 1.5
        private const val MIN_SCALE: Int = 0
        private const val MAX_SCALE: Int = 12

        private val colorRgbaGreen: ImVec4 = ImVec4(0f, 1f, 0f, 1f)
        private val colorRgbaRed: ImVec4 = ImVec4(1f, 0f, 0f, 1f)
    }

    private lateinit var providedPreferences: Preferences

    private val renderDataStorageByMapId: MutableMap<Int, RenderData> = mutableMapOf()
    private lateinit var renderData: RenderData

    private var selectedTileItem: TileItem? = null

    private var isCanvasBlocked: Boolean = false
    private var isTilePopupOpened: Boolean = false

    private var isHasMap: Boolean = false
    private var iconSize: Int = DEFAULT_ICON_SIZE

    private var maxX: Int = OUT_OF_BOUNDS
    private var maxY: Int = OUT_OF_BOUNDS

    private val doFrameAreas: ImBoolean = ImBoolean(true)
    private val doSynchronizeMapsView: ImBoolean = ImBoolean(false)

    // Tile of the map covered with mouse
    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    private var isMapMouseDragged: Boolean = false
    private var isMovingCanvasWithLmb: Boolean = false

    // To handle user input
    private val mousePos: ImVec2 = ImVec2()
    private val mouseDelta: ImVec2 = ImVec2()

    private val canvasRenderer = CanvasRenderer()

    private val shortcutHandler = ShortcutHandler()

    init {
        EventBus.sign(Reaction.ApplicationBlockChanged::class.java, ::handleApplicationBlockChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapSizeChanged::class.java, ::handleSelectedMapSizeChanged)
        EventBus.sign(ReactionMapHolderService.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        EventBus.sign(ReactionEnvironmentService.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionCanvasService.FrameRefreshed::class.java, ::handleFrameRefreshed)
        EventBus.sign(ReactionTileItemService.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        EventBus.sign(ReactionTilePopupUi.TilePopupOpened::class.java, ::handleTilePopupOpened)
        EventBus.sign(ReactionTilePopupUi.TilePopupClosed::class.java, ::handleTilePopupClosed)
        EventBus.sign(ProviderFrameService.ComposedFrame::class.java, ::handleProviderComposedFrame)
        EventBus.sign(ProviderFrameService.FramedTiles::class.java, ::handleProviderFramedTiles)
        EventBus.sign(ProviderPreferencesService.Preferences::class.java, ::handleProviderPreferences)
        EventBus.sign(TriggerCanvasService.CenterCanvasByPosition::class.java, ::handleCenterCanvasByPosition)
        EventBus.sign(TriggerCanvasService.MarkPosition::class.java, ::handleMarkPosition)
        EventBus.sign(TriggerCanvasService.ResetMarkedPosition::class.java, ::handleResetMarkedPosition)
        EventBus.sign(TriggerCanvasService.SelectTiles::class.java, ::handleSelectTiles)
        EventBus.sign(TriggerCanvasService.ResetSelectedTiles::class.java, ::handleResetSelectedTiles)
        EventBus.sign(TriggerCanvasService.SelectArea::class.java, ::handleSelectArea)
        EventBus.sign(TriggerCanvasService.ResetSelectedArea::class.java, ::handleResetSelectedArea)

        shortcutHandler.addShortcut(Shortcut.PLUS_PAIR, action = ::zoomIn)
        shortcutHandler.addShortcut(Shortcut.MINUS_PAIR, action = ::zoomOut)
        shortcutHandler.addShortcut(GLFW.GLFW_KEY_UP, action = ::translateCanvasUp)
        shortcutHandler.addShortcut(GLFW.GLFW_KEY_DOWN, action = ::translateCanvasDown)
        shortcutHandler.addShortcut(GLFW.GLFW_KEY_LEFT, action = ::translateCanvasLeft)
        shortcutHandler.addShortcut(GLFW.GLFW_KEY_RIGHT, action = ::translateCanvasRight)
    }

    override fun postInit() {
        EventBus.post(ProviderCanvasService.DoFrameAreas(doFrameAreas))
        EventBus.post(ProviderCanvasService.DoSynchronizeMapsView(doSynchronizeMapsView))
    }

    override fun process() {
        if (!isHasMap) {
            return
        }

        ImGui.getMousePos(mousePos)

        if (!isCanvasBlocked && !isImGuiInUse()) {
            processMouseCursor()
            processViewDrag()
            processViewScroll()
            processTilePopupClick()
            processMapMouseDrag()
            processMapMousePosition()
            processTileItemSelectMode()
        }

        prepareCanvasRenderer()
        canvasRenderer.render()

        postProcessTileItemSelectMode()
        postProcessCanvasMovingChecks()
        postProcessSynchronizeMapsView()
    }

    private fun processMouseCursor() {
        if (ImGui.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeAll)
        }
    }

    private fun processViewDrag() {
        if (!(ImGui.isMouseDown(ImGuiMouseButton.Middle) || (ImGui.isKeyDown(GLFW.GLFW_KEY_SPACE) && ImGui.isMouseDown(ImGuiMouseButton.Left)))) {
            return
        }

        if (ImGui.isKeyDown(GLFW.GLFW_KEY_SPACE) && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            isMovingCanvasWithLmb = true
        }

        ImGui.getIO().getMouseDelta(mouseDelta)

        if (mouseDelta.x != 0f || mouseDelta.y != 0f) {
            translateCanvas(-mouseDelta.x, mouseDelta.y)
        }
    }

    private fun processViewScroll() {
        val mouseWheel = ImGui.getIO().mouseWheel

        if (mouseWheel == 0f) {
            return
        }

        fun translate() {
            if (ImGui.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || ImGui.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                translateCanvas(getManualTranslateValue(mouseWheel), 0f)
            } else {
                translateCanvas(0f, getManualTranslateValue(mouseWheel))
            }
        }

        fun zoom() {
            zoom(mouseWheel > 0)
        }

        if (providedPreferences.alternativeScrollBehavior.getValue().data && !ImGui.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
            translate()
        } else {
            zoom()
        }
    }

    private fun processTilePopupClick() {
        if (isTilePopupOpened && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            EventBus.post(TriggerTilePopupUi.Close())
        }

        if (!ImGui.isMouseClicked(ImGuiMouseButton.Right) || ImGui.getIO().keyShift) {
            return
        }

        EventBus.post(TriggerMapHolderService.FetchSelectedMap {
            if (xMapMousePos != OUT_OF_BOUNDS && yMapMousePos != OUT_OF_BOUNDS) {
                EventBus.post(TriggerTilePopupUi.Open(it.getTile(xMapMousePos, yMapMousePos, it.zSelected)))
            }
        })
    }

    private fun processMapMouseDrag() {
        if (isMovingCanvasWithLmb || isTilePopupOpened || ImGui.getIO().keyShift) {
            return
        }

        if (ImGui.isMouseDown(ImGuiMouseButton.Left) && !isMapMouseDragged) {
            isMapMouseDragged = true
            EventBus.post(ReactionCanvasService.MapMouseDragStarted.SIGNAL)
        } else if (!ImGui.isMouseDown(ImGuiMouseButton.Left) && isMapMouseDragged) {
            isMapMouseDragged = false
            EventBus.post(ReactionCanvasService.MapMouseDragStopped.SIGNAL)
        }
    }

    private fun processMapMousePosition() {
        val x = mousePos.x
        val y = mousePos.y

        val xMap = (x * renderData.viewScale - renderData.viewTranslateX) / iconSize
        val yMap = ((Window.windowHeight - y) * renderData.viewScale - renderData.viewTranslateY) / iconSize

        val xMapMousePosNew = if (xMap > 0 && xMap <= maxX) xMap.toInt() + 1 else OUT_OF_BOUNDS
        val yMapMousePosNew = if (yMap > 0 && yMap <= maxY) yMap.toInt() + 1 else OUT_OF_BOUNDS

        if (xMapMousePos != xMapMousePosNew || yMapMousePos != yMapMousePosNew) {
            xMapMousePos = xMapMousePosNew
            yMapMousePos = yMapMousePosNew
            EventBus.post(ReactionCanvasService.MapMousePosChanged(MapPos(xMapMousePos, yMapMousePos)))
        }
    }

    private fun processTileItemSelectMode() {
        if (ImGui.getIO().keyShift) {
            canvasRenderer.isTileItemSelectMode = true
            canvasRenderer.tileItemSelectColor = if (ImGui.getIO().keyCtrl) colorRgbaRed else colorRgbaGreen
        }
    }

    private fun prepareCanvasRenderer() {
        canvasRenderer.renderData = renderData
        canvasRenderer.xMapMousePos = xMapMousePos
        canvasRenderer.yMapMousePos = yMapMousePos
        canvasRenderer.iconSize = iconSize
        canvasRenderer.realIconSize = (iconSize / renderData.viewScale).toInt()
        canvasRenderer.mousePosX = mousePos.x * renderData.viewScale.toFloat()
        canvasRenderer.mousePosY = (Window.windowHeight - mousePos.y) * renderData.viewScale.toFloat()
        canvasRenderer.frameAreas = doFrameAreas.get()
    }

    private fun postProcessTileItemSelectMode() {
        if (!canvasRenderer.isTileItemSelectMode) {
            return
        }

        if (canvasRenderer.tileItemIdMouseOver != 0L) {
            if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
                if (ImGui.getIO().keyCtrl) { // Delete tile item
                    EventBus.post(TriggerMapHolderService.FetchSelectedMap { currentMap ->
                        deleteTileItemUnderMouse(currentMap)
                    })
                } else { // Select tile item
                    EventBus.post(TriggerTileItemService.ChangeSelectedTileItem(GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)))
                }
            } else if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                EventBus.post(TriggerMapHolderService.FetchSelectedMap { currentMap ->
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

        EventBus.post(
            TriggerActionService.QueueUndoable(
                ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem, selectedTileItem!!)
                }
            )
        )

        EventBus.post(TriggerFrameService.RefreshFrame())
    }

    private fun deleteTileItemUnderMouse(map: Dmm) {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val x = canvasRenderer.xForTileItemMouseOver
        val y = canvasRenderer.yForTileItemMouseOver
        val tile = map.getTile(x, y, map.zSelected)

        EventBus.post(
            TriggerActionService.QueueUndoable(
                ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem)
                }
            )
        )

        EventBus.post(TriggerFrameService.RefreshFrame())
    }

    private fun openTileItemUnderMouseForEdit(map: Dmm) {
        val tileItem = GlobalTileItemHolder.getById(canvasRenderer.tileItemIdMouseOver)
        val x = canvasRenderer.xForTileItemMouseOver
        val y = canvasRenderer.yForTileItemMouseOver
        val tile = map.getTile(x, y, map.zSelected)
        val tileItemIdx = tile.getTileItemIdx(tileItem)

        EventBus.post(TriggerEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
    }

    private fun translateCanvas(xShift: Float, yShift: Float) {
        if (!isHasMap) {
            return
        }

        canvasRenderer.run {
            renderData.viewTranslateX -= xShift * renderData.viewScale
            renderData.viewTranslateY -= yShift * renderData.viewScale
            redraw = true
        }

        EventBus.post(TriggerTilePopupUi.Close())
    }

    private fun getManualTranslateValue(modifier: Float = 1f): Float {
        return if (ImGui.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || ImGui.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            modifier * iconSize.toFloat() * 5f
        } else {
            modifier * iconSize.toFloat()
        }
    }

    private fun translateCanvasUp() {
        translateCanvas(0f, getManualTranslateValue())
    }

    private fun translateCanvasDown() {
        translateCanvas(0f, -getManualTranslateValue())
    }

    private fun translateCanvasLeft() {
        translateCanvas(-getManualTranslateValue(), 0f)
    }

    private fun translateCanvasRight() {
        translateCanvas(getManualTranslateValue(), 0f)
    }

    private fun zoom(isZoomIn: Boolean) {
        if (!isHasMap) {
            return
        }

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

        EventBus.post(TriggerTilePopupUi.Close())
    }

    private fun zoomIn() {
        zoom(true)
    }

    private fun zoomOut() {
        zoom(false)
    }

    private fun postProcessCanvasMovingChecks() {
        if (isMovingCanvasWithLmb && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            isMovingCanvasWithLmb = false
        }
    }

    private fun postProcessSynchronizeMapsView() {
        if (doSynchronizeMapsView.get()) {
            renderDataStorageByMapId.filterNot { it.value === renderData }.forEach { (_, data) ->
                data.viewScale = renderData.viewScale
                data.scaleCount = renderData.scaleCount
                data.viewTranslateX = renderData.viewTranslateX
                data.viewTranslateY = renderData.viewTranslateY
            }
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

    private fun handleSelectedMapSizeChanged(event: Event<MapSize, Unit>) {
        maxX = event.body.maxX
        maxY = event.body.maxY
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        iconSize = event.body.getWorldIconSize()
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
        isTilePopupOpened = true
    }

    private fun handleTilePopupClosed() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                isTilePopupOpened = false
            }
        }, 150)
    }

    private fun handleProviderComposedFrame(event: Event<List<FrameMesh>, Unit>) {
        canvasRenderer.providedComposedFrame = event.body
    }

    private fun handleProviderFramedTiles(event: Event<List<FramedTile>, Unit>) {
        canvasRenderer.providedFramedTiles = event.body
    }

    private fun handleProviderPreferences(event: Event<Preferences, Unit>) {
        providedPreferences = event.body
    }

    private fun handleCenterCanvasByPosition(event: Event<MapPos, Unit>) {
        renderData.viewTranslateX = Window.windowWidth / 2 * renderData.viewScale + (event.body.x - 1) * iconSize * -1.0
        renderData.viewTranslateY = Window.windowHeight / 2 * renderData.viewScale + (event.body.y - 1) * iconSize * -1.0
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
