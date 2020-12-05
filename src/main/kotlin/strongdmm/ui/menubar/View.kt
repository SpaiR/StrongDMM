package strongdmm.ui.menubar

import imgui.ImGui
import strongdmm.util.imgui.imGuiMainMenuBar
import strongdmm.util.imgui.imGuiMenu
import strongdmm.util.imgui.imGuiMenuItem
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    lateinit var viewController: ViewController

    fun process() {
        imGuiMainMenuBar {
            imGuiMenu("File") {
                imGuiMenuItem("Open Environment...", enabled = !state.isLoadingEnvironment, block = viewController::doOpenEnvironment)
                imGuiMenu("Recent Environments", enabled = !state.isLoadingEnvironment) {
                    showRecentEnvironments()
                }

                ImGui.separator()

                imGuiMenuItem("New Map...", shortcut = "Ctrl+N", enabled = state.isEnvironmentOpened, block = viewController::doNewMap)
                imGuiMenuItem("Open Map...", shortcut = "Ctrl+O", enabled = state.isEnvironmentOpened, block = viewController::doOpenMap)
                imGuiMenuItem("Open Available Map...", shortcut = "Ctrl+Shift+O", enabled = state.isEnvironmentOpened, block = viewController::doOpenAvailableMap)
                imGuiMenu("Recent Maps", enabled = state.isEnvironmentOpened) {
                    showRecentMaps()
                }

                ImGui.separator()

                imGuiMenuItem("Close Map", shortcut = "Ctrl+W", enabled = state.isMapOpened, block = viewController::doCloseMap)
                imGuiMenuItem("Close All Maps", shortcut = "Ctrl+Shift+W", enabled = state.isMapOpened, block = viewController::doCloseAllMaps)

                ImGui.separator()

                imGuiMenuItem("Save", shortcut = "Ctrl+S", enabled = state.isMapOpened, block = viewController::doSave)
                imGuiMenuItem("Save All", shortcut = "Ctrl+Shift+S", enabled = state.isMapOpened, block = viewController::doSaveAll)
                imGuiMenuItem("Save As...", enabled = state.isMapOpened, block = viewController::doSaveAs)

                ImGui.separator()

                imGuiMenuItem("Preferences...", block = viewController::doOpenPreferences)

                ImGui.separator()

                imGuiMenuItem("Exit", shortcut = "Ctrl+Q", block = viewController::doExit)
            }

            imGuiMenu("Edit") {
                imGuiMenuItem("Undo", shortcut = "Ctrl+Z", enabled = state.isUndoEnabled, block = viewController::doUndo)
                imGuiMenuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = state.isRedoEnabled, block = viewController::doRedo)

                ImGui.separator()

                imGuiMenuItem("Cut", shortcut = "Ctrl+X", enabled = state.isMapOpened, block = viewController::doCut)
                imGuiMenuItem("Copy", shortcut = "Ctrl+C", enabled = state.isMapOpened, block = viewController::doCopy)
                imGuiMenuItem("Paste", shortcut = "Ctrl+V", enabled = state.isMapOpened, block = viewController::doPaste)
                imGuiMenuItem("Delete", shortcut = "Delete", enabled = state.isMapOpened, block = viewController::doDelete)
                imGuiMenuItem("Deselect All", shortcut = "Ctrl+D", block = viewController::doDeselectAll)

                ImGui.separator()

                imGuiMenuItem("Set Map Size...", enabled = state.isMapOpened, block = viewController::doSetMapSize)
                imGuiMenuItem("Find Instance...", shortcut = "Ctrl+F", block = viewController::doFindInstance)
            }

            imGuiMenu("Options") {
                imGuiMenuItem("Layers Filter...", enabled = state.isEnvironmentOpened, block = viewController::doOpenLayersFilter)

                imGuiMenuItem(
                    "Toggle Area",
                    shortcut = "Ctrl+1",
                    enabled = state.isEnvironmentOpened,
                    selected = state.isAreaLayerActive,
                    block = viewController::toggleAreaLayer
                )

                imGuiMenuItem(
                    "Toggle Turf",
                    shortcut = "Ctrl+2",
                    enabled = state.isEnvironmentOpened,
                    selected = state.isTurfLayerActive,
                    block = viewController::toggleTurfLayer
                )

                imGuiMenuItem(
                    "Toggle Object",
                    shortcut = "Ctrl+3",
                    enabled = state.isEnvironmentOpened,
                    selected = state.isObjLayerActive,
                    block = viewController::toggleObjLayer
                )

                imGuiMenuItem(
                    "Toggle Mob",
                    shortcut = "Ctrl+4",
                    enabled = state.isEnvironmentOpened,
                    selected = state.isMobLayerActive,
                    block = viewController::toggleMobLayer
                )

                ImGui.separator()

                imGuiMenuItem("Frame Areas", selected = state.providedDoFrameAreas, block = {})
                imGuiMenuItem("Synchronize Maps View", selected = state.providedDoSynchronizeMapsView, block = {})

                ImGui.separator()

                imGuiMenuItem("Screenshot...", block = viewController::doScreenshot)
            }

            imGuiMenu("Window") {
                imGuiMenuItem("Reset Windows", shortcut = "F5", block = viewController::doResetWindows)
                imGuiMenuItem("Fullscreen", shortcut = "F11", selected = Window.isFullscreen, block = viewController::doFullscreen)
            }

            imGuiMenu("Help") {
                imGuiMenuItem("Changelog...", block = viewController::doChangelog)
                imGuiMenuItem("About...", block = viewController::doAbout)
            }
        }
    }

    private fun showRecentEnvironments() {
        if (state.providedRecentEnvironments.isEmpty()) {
            return
        }

        state.providedRecentEnvironments.toTypedArray().forEach { recentEnvironmentPath ->
            imGuiMenuItem(recentEnvironmentPath) {
                viewController.doOpenRecentEnvironment(recentEnvironmentPath)
            }
        }

        ImGui.separator()

        imGuiMenuItem("Clear Recent Environments", block = viewController::doClearRecentEnvironments)
    }

    private fun showRecentMaps() {
        if (state.providedRecentMaps.isEmpty()) {
            return
        }

        state.providedRecentMaps.toTypedArray().forEach { (readable, absolute) ->
            imGuiMenuItem(readable) {
                viewController.doOpenRecentMap(absolute)
            }
        }

        ImGui.separator()

        imGuiMenuItem("Clear Recent Maps", block = viewController::doClearRecentMaps)
    }
}
