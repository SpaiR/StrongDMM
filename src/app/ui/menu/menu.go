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
	DoOpenEnvironment()
	DoOpenEnvironmentByPath(path string)
	DoClearRecentEnvironments()
	DoOpenMap() // Ctrl+O
	DoOpenMapByPath(path string)
	DoClearRecentMaps()
	DoSave() // Ctrl+S
	DoOpenPreferences()
	DoExit()

	// Edit
	DoUndo()     // Ctrl+Z
	DoRedo()     // Ctrl+Shift+Z | Ctrl+Y
	DoCopy()     // Ctrl+C
	DoPaste()    // Ctrl+V
	DoCut()      // Ctrl+X
	DoDelete()   // Delete
	DoSearch()   // Ctrl+F
	DoDeselect() // Ctrl+D

	// Window
	DoResetLayout() // F5

	// Help
	DoOpenLogs()

	// Helpers

	RecentEnvironments() []string
	RecentMapsByLoadedEnvironment() []string

	LoadedEnvironment() *dmenv.Dme
	HasLoadedEnvironment() bool

	HasActiveMap() bool

	PathsFilter() *dm.PathsFilter
	CommandStorage() *command.Storage
	Clipboard() *dmmclip.Clipboard
}

type Menu struct {
	app app

	shortcuts shortcut.Shortcuts
}

func New(app app) *Menu {
	m := &Menu{app: app}
	m.addShortcuts()
	return m
}

func (m *Menu) Process() {
	w.MainMenuBar(w.Layout{
		w.Menu("File", w.Layout{
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
			w.MenuItem("Open Map...", m.app.DoOpenMap).
				Icon(icon.FaFolderOpen).
				Enabled(m.app.HasLoadedEnvironment()).
				Shortcut("Ctrl+O"),
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
			w.MenuItem("Save", m.app.DoSave).
				Icon(icon.FaSave).
				Enabled(m.app.HasActiveMap()).
				Shortcut("Ctrl+S"),
			w.Separator(),
			w.MenuItem("Preferences", m.app.DoOpenPreferences).
				Icon(icon.FaWrench),
			w.Separator(),
			w.MenuItem("Exit", m.app.DoExit).
				IconEmpty(),
		}),

		w.Menu("Edit", w.Layout{
			w.MenuItem("Undo", m.app.DoUndo).
				Icon(icon.FaUndo).
				Enabled(m.app.CommandStorage().HasUndo()).
				Shortcut("Ctrl+Z"),
			w.MenuItem("Redo", m.app.DoRedo).
				Icon(icon.FaRedo).
				Enabled(m.app.CommandStorage().HasRedo()).
				Shortcut("Ctrl+Shift+Z"),
			w.Separator(),
			w.MenuItem("Copy", m.app.DoCopy).
				Icon(icon.FaCopy).
				Shortcut("Ctrl+C"),
			w.MenuItem("Paste", m.app.DoPaste).
				Icon(icon.FaPaste).
				Enabled(m.app.Clipboard().HasData()).
				Shortcut("Ctrl+V"),
			w.MenuItem("Cut", m.app.DoCut).
				Icon(icon.FaCut).
				Shortcut("Ctrl+X"),
			w.MenuItem("Delete", m.app.DoDelete).
				Icon(icon.FaEraser).
				Shortcut("Delete"),
			w.MenuItem("Deselect", m.app.DoDeselect).
				IconEmpty().
				Shortcut("Ctrl+D"),
			w.Separator(),
			w.MenuItem("Search", m.app.DoSearch).
				Icon(icon.FaSearch).
				Enabled(m.app.HasActiveMap()).
				Shortcut("Ctrl+F"),
		}),

		w.Menu("Options", w.Layout{
			w.MenuItem("Toggle Area", m.doToggleArea).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isAreaToggled()).
				Shortcut("Ctrl+1"),
			w.MenuItem("Toggle Turf", m.doToggleTurf).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isTurfToggled()).
				Shortcut("Ctrl+2"),
			w.MenuItem("Toggle Object", m.doToggleObject).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isObjectToggled()).
				Shortcut("Ctrl+3"),
			w.MenuItem("Toggle Mob", m.doToggleMob).
				IconEmpty().
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isMobToggled()).
				Shortcut("Ctrl+4"),
		}),

		w.Menu("Window", w.Layout{
			w.MenuItem("Reset Layout", m.app.DoResetLayout).Shortcut("F5").
				Icon(icon.FaWindowRestore),
		}),

		w.Menu("Help", w.Layout{
			w.MenuItem("Open Logs Folder", m.app.DoOpenLogs).
				IconEmpty(),
		}),
	}).Build()
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
