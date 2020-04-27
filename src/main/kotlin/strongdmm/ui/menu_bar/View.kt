package strongdmm.ui.menu_bar

import imgui.ImGui.*
import strongdmm.util.imgui.mainMenuBar
import strongdmm.util.imgui.menu
import strongdmm.util.imgui.menuItem

class View(
    private val state: State
) {
    lateinit var viewController: ViewController

    fun process() {
        mainMenuBar {
            menu("File") {
                menuItem("Open Environment...", enabled = state.progressText == null, block = viewController::doOpenEnvironment)
                menu("Recent Environments", enabled = state.progressText == null) {
                    showRecentEnvironments()
                }

                separator()

                menuItem("New Map...", shortcut = "Ctrl+N", enabled = state.isEnvironmentOpened, block = viewController::doNewMap)
                menuItem("Open Map...", shortcut = "Ctrl+O", enabled = state.isEnvironmentOpened, block = viewController::doOpenMap)
                menuItem("Open Available Map", shortcut = "Ctrl+Shift+O", enabled = state.isEnvironmentOpened, block = viewController::doOpenAvailableMap)
                menu("Recent Maps", enabled = state.isEnvironmentOpened) {
                    showRecentMaps()
                }

                separator()

                menuItem("Close Map", shortcut = "Ctrl+W", enabled = state.isMapOpened, block = viewController::doCloseMap)
                menuItem("Close All Maps", shortcut = "Ctrl+Shift+W", enabled = state.isMapOpened, block = viewController::doCloseAllMaps)
                separator()

                menuItem("Save", shortcut = "Ctrl+S", enabled = state.isMapOpened, block = viewController::doSave)
                menuItem("Save All", shortcut = "Ctrl+Shift+S", enabled = state.isMapOpened, block = viewController::doSaveAll)
                menuItem("Save As...", enabled = state.isMapOpened, block = viewController::doSaveAs)

                separator()

                menuItem("Exit", shortcut = "Ctrl+Q", block = viewController::doExit)
            }

            menu("Edit") {
                menuItem("Undo", shortcut = "Ctrl+Z", enabled = state.isUndoEnabled, block = viewController::doUndo)
                menuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = state.isRedoEnabled, block = viewController::doRedo)

                separator()

                menuItem("Cut", shortcut = "Ctrl+X", enabled = state.isMapOpened, block = viewController::doCut)
                menuItem("Copy", shortcut = "Ctrl+C", enabled = state.isMapOpened, block = viewController::doCopy)
                menuItem("Paste", shortcut = "Ctrl+V", enabled = state.isMapOpened, block = viewController::doPaste)
                menuItem("Delete", shortcut = "Delete", enabled = state.isMapOpened, block = viewController::doDelete)
                menuItem("Deselect All", shortcut = "Ctrl+D", block = viewController::doDeselectAll)

                separator()

                menuItem("Set Map Size...", enabled = state.isMapOpened, block = viewController::doSetMapSize)
                menuItem("Find Instance...", shortcut = "Ctrl+F", block = viewController::doFindInstance)
            }

            menu("Options") {
                menuItem("Layers Filter", enabled = state.isEnvironmentOpened, block = viewController::doOpenLayersFilter)

                menuItem(
                    "Toggle Area",
                    shortcut = "Ctrl+1",
                    enabled = state.isEnvironmentOpened,
                    selected = state.isAreaLayerActive,
                    block = viewController::toggleAreaLayer
                )

                menuItem(
                    "Toggle Turf",
                    shortcut = "Ctrl+2",
                    enabled = state.isEnvironmentOpened,
                    selected = state.isTurfLayerActive,
                    block = viewController::toggleTurfLayer
                )

                menuItem(
                    "Toggle Object",
                    shortcut = "Ctrl+3",
                    enabled = state.isEnvironmentOpened,
                    selected = state.isObjLayerActive,
                    block = viewController::toggleObjLayer
                )

                menuItem(
                    "Toggle Mob",
                    shortcut = "Ctrl+4",
                    enabled = state.isEnvironmentOpened,
                    selected = state.isMobLayerActive,
                    block = viewController::toggleMobLayer
                )

                separator()

                menuItem("Frame Areas", selected = state.providedFrameAreas, block = {})
                menuItem("Preferences...", block = viewController::doOpenPreferences)
            }

            menu("Window") {
                menuItem("Reset Windows", block = viewController::doResetWindows)
            }

            menu("Help") {
                menuItem("Changelog", block = viewController::doChangelog)
                menuItem("About", block = viewController::doAbout)
            }

            state.progressText?.let {
                val count = (getTime() / 0.25).toInt() and 3
                val bar = charArrayOf('|', '/', '-', '\\')
                text("${bar[count]} $it${".".repeat(count)}")
            }
        }
    }

    private fun showRecentEnvironments() {
        if (state.providedRecentEnvironments.isEmpty()) {
            return
        }

        state.providedRecentEnvironments.toTypedArray().forEach { recentEnvironmentPath ->
            menuItem(recentEnvironmentPath) {
                viewController.doOpenRecentEnvironment(recentEnvironmentPath)
            }
        }

        separator()

        menuItem("Clear Recent Environments", block = viewController::doClearRecentEnvironments)
    }

    private fun showRecentMaps() {
        if (state.providedRecentMaps.isEmpty()) {
            return
        }

        state.providedRecentMaps.toTypedArray().forEach { (readable, absolute) ->
            menuItem(readable) {
                viewController.doOpenRecentMap(absolute)
            }
        }

        separator()

        menuItem("Clear Recent Maps", block = viewController::doClearRecentMaps)
    }
}
