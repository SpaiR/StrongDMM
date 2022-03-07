package menu

import (
	"sdmm/app/command"
	"sdmm/app/ui/shortcut"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmclip"
	"sdmm/imguiext/icon"
	w "sdmm/imguiext/widget"
)

//goland:noinspection GoCommentStart
type app interface {
	// File
	DoNewWorkspace()
	DoOpenEnvironment()
	DoOpenEnvironmentByPath(path string)
	DoClearRecentEnvironments()
	DoNewMap()
	DoOpenMap()
	DoOpenMapByPath(path string)
	DoClearRecentMaps()
	DoClose()
	DoCloseAll()
	DoSave()
	DoOpenPreferences()
	DoExit()

	// Edit
	DoUndo()
	DoRedo()
	DoCopy()
	DoPaste()
	DoCut()
	DoDelete()
	DoSearch()
	DoDeselect()

	// Options
	DoAreaBorders()
	DoMultiZRendering()
	DoMirrorCanvasCamera()

	// Window
	DoResetLayout()

	// Help
	DoOpenChangelog()
	DoOpenAbout()
	DoOpenLogs()
	DoOpenSourceCode()

	// Other
	DoSelfUpdate()
	DoRestart()

	// Helpers

	RecentEnvironments() []string
	RecentMapsByLoadedEnvironment() []string

	LoadedEnvironment() *dmenv.Dme
	HasLoadedEnvironment() bool

	HasActiveMap() bool

	PathsFilter() *dm.PathsFilter
	CommandStorage() *command.Storage
	Clipboard() *dmmclip.Clipboard

	AreaBordersRendering() bool
	MultiZRendering() bool
	MirrorCanvasCamera() bool
}

type upStatus int

const (
	upStatusNone upStatus = iota
	upStatusAvailable
	upStatusUpdating
	upStatusUpdated
	upStatusError
)

type Menu struct {
	app app

	shortcuts shortcut.Shortcuts

	updateStatus      upStatus
	updateVersion     string
	updateDescription string
}

func New(app app) *Menu {
	m := &Menu{app: app}
	m.addShortcuts()
	return m
}

func (m *Menu) Process() {
	w.MainMenuBar(w.Layout{
		w.Menu("File", w.Layout{
			w.MenuItem("New Workspace", m.app.DoNewWorkspace).
				Icon(icon.FaFile).
				Shortcut(shortcut.KeyModName(), "N"),
			w.Separator(),
			w.MenuItem("Open Environment...", m.app.DoOpenEnvironment).
				Icon(icon.FaFolderOpen),
			w.Menu("Recent Environments", w.Layout{
				w.Custom(func() {
					for _, recentEnvironment := range m.app.RecentEnvironments() {
						w.MenuItem(recentEnvironment, func() {
							m.app.DoOpenEnvironmentByPath(recentEnvironment)
						}).IconEmpty().Build()
					}
					w.Layout{
						w.Separator(),
						w.MenuItem("Clear Recent Environments", m.app.DoClearRecentEnvironments).
							Icon(icon.FaTrash),
					}.Build()
				}),
			}).IconEmpty().Enabled(len(m.app.RecentEnvironments()) != 0),
			w.Separator(),
			w.MenuItem("New Map", m.app.DoNewMap).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()),
			w.MenuItem("Open Map...", m.app.DoOpenMap).
				Icon(icon.FaFolderOpen).
				Enabled(m.app.HasLoadedEnvironment()).
				Shortcut(shortcut.KeyModName(), "O"),
			w.Menu("Recent Maps", w.Layout{
				w.Custom(func() {
					for _, recentMap := range m.app.RecentMapsByLoadedEnvironment() {
						w.MenuItem(recentMap, func() {
							m.app.DoOpenMapByPath(recentMap)
						}).IconEmpty().Build()
					}
					w.Layout{
						w.Separator(),
						w.MenuItem("Clear Recent Maps", m.app.DoClearRecentMaps).
							Icon(icon.FaTrash),
					}.Build()
				}),
			}).IconEmpty().Enabled(m.app.HasLoadedEnvironment() && len(m.app.RecentMapsByLoadedEnvironment()) != 0),
			w.Separator(),
			w.MenuItem("Close", m.app.DoClose).
				Shortcut(shortcut.KeyModName(), "W"),
			w.MenuItem("Close All", m.app.DoCloseAll).
				Shortcut(shortcut.KeyModName(), "Shift", "W"),
			w.Separator(),
			w.MenuItem("Save", m.app.DoSave).
				Icon(icon.FaSave).
				Enabled(m.app.HasActiveMap()).
				Shortcut(shortcut.KeyModName(), "S"),
			w.Separator(),
			w.MenuItem("Preferences", m.app.DoOpenPreferences).
				Icon(icon.FaWrench),
			w.Separator(),
			w.MenuItem("Exit", m.app.DoExit).
				IconEmpty().
				Shortcut(shortcut.Combine(shortcut.KeyModName(), "Q")),
		}),

		w.Menu("Edit", w.Layout{
			w.MenuItem("Undo", m.app.DoUndo).
				Icon(icon.FaUndo).
				Enabled(m.app.CommandStorage().HasUndo()).
				Shortcut(shortcut.KeyModName(), "Z"),
			w.MenuItem("Redo", m.app.DoRedo).
				Icon(icon.FaRedo).
				Enabled(m.app.CommandStorage().HasRedo()).
				Shortcut(shortcut.KeyModName(), "Shift", "Z"),
			w.Separator(),
			w.MenuItem("Copy", m.app.DoCopy).
				Icon(icon.FaCopy).
				Shortcut(shortcut.KeyModName(), "C"),
			w.MenuItem("Paste", m.app.DoPaste).
				Icon(icon.FaPaste).
				Enabled(m.app.Clipboard().HasData()).
				Shortcut(shortcut.KeyModName(), "V"),
			w.MenuItem("Cut", m.app.DoCut).
				Icon(icon.FaCut).
				Shortcut(shortcut.KeyModName(), "X"),
			w.MenuItem("Delete", m.app.DoDelete).
				Icon(icon.FaEraser).
				Shortcut("Delete"),
			w.MenuItem("Deselect", m.app.DoDeselect).
				IconEmpty().
				Shortcut(shortcut.KeyModName(), "D"),
			w.Separator(),
			w.MenuItem("Search", m.app.DoSearch).
				Icon(icon.FaSearch).
				Enabled(m.app.HasActiveMap()).
				Shortcut(shortcut.KeyModName(), "F"),
		}),

		w.Menu("Options", w.Layout{
			w.MenuItem("Show Area", m.doToggleArea).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isAreaToggled()).
				Shortcut(shortcut.KeyModName(), "1"),
			w.MenuItem("Show Turf", m.doToggleTurf).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isTurfToggled()).
				Shortcut(shortcut.KeyModName(), "2"),
			w.MenuItem("Show Object", m.doToggleObject).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isObjectToggled()).
				Shortcut(shortcut.KeyModName(), "3"),
			w.MenuItem("Show Mob", m.doToggleMob).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isMobToggled()).
				Shortcut(shortcut.KeyModName(), "4"),
			w.MenuItem("Show All", m.doShowAll).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()),
			w.Separator(),
			w.MenuItem("Area Borders", m.app.DoAreaBorders).
				IconEmpty().
				Selected(m.app.AreaBordersRendering()),
			w.MenuItem("Multi-Z Rendering", m.app.DoMultiZRendering).
				IconEmpty().
				Selected(m.app.MultiZRendering()).
				Shortcut(shortcut.KeyModName(), "0"),
			w.MenuItem("Mirror Canvas Camera", m.app.DoMirrorCanvasCamera).
				IconEmpty().
				Selected(m.app.MirrorCanvasCamera()),
		}),

		w.Menu("Window", w.Layout{
			w.MenuItem("Reset Layout", m.app.DoResetLayout).Shortcut("F5").
				Icon(icon.FaWindowRestore),
		}),

		w.Menu("Help", w.Layout{
			w.MenuItem("Changelog", m.app.DoOpenChangelog).
				Icon(icon.FaClipboardList),
			w.MenuItem("About", m.app.DoOpenAbout).
				IconEmpty(),
			w.MenuItem("Source Code", m.app.DoOpenSourceCode).
				IconEmpty(),
			w.Separator(),
			w.MenuItem("Open Logs Folder", m.app.DoOpenLogs).
				IconEmpty(),
		}),

		w.Custom(func() {
			if m.updateStatus != upStatusNone {
				m.showUpdateMenu()
			}
		}),
	}).Build()
}

func (m *Menu) SetUpdateAvailable(version, description string) {
	m.updateStatus = upStatusAvailable
	m.updateVersion = version
	m.updateDescription = description
}

func (m *Menu) SetUpdating() {
	m.updateStatus = upStatusUpdating
}

func (m *Menu) SetUpdated() {
	m.updateStatus = upStatusUpdated
}

func (m *Menu) SetUpdateError() {
	m.updateStatus = upStatusError
}

func (m *Menu) doToggleArea() {
	m.app.PathsFilter().TogglePath("/area")
}

func (m *Menu) doToggleTurf() {
	m.app.PathsFilter().TogglePath("/turf")
}

func (m *Menu) doToggleObject() {
	m.app.PathsFilter().TogglePath("/obj")
}

func (m *Menu) doToggleMob() {
	m.app.PathsFilter().TogglePath("/mob")
}

func (m *Menu) doShowAll() {
	m.app.PathsFilter().Clear()
}

func (m *Menu) isAreaToggled() bool {
	return m.app.PathsFilter().IsVisiblePath("/area")
}

func (m *Menu) isTurfToggled() bool {
	return m.app.PathsFilter().IsVisiblePath("/turf")
}

func (m *Menu) isObjectToggled() bool {
	return m.app.PathsFilter().IsVisiblePath("/obj")
}

func (m *Menu) isMobToggled() bool {
	return m.app.PathsFilter().IsVisiblePath("/mob")
}
