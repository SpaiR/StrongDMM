package menu

import (
	"sdmm/dm"
	"sdmm/dm/dmenv"
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
	DoUndo() // Ctrl+Z
	DoRedo() // Ctrl+Shift+Z | Ctrl+Y

	// Window
	DoResetWindows() // F5

	// Help
	DoOpenLogs()

	// Helpers

	RecentEnvironments() []string
	RecentMapsByLoadedEnvironment() []string

	LoadedEnvironment() *dmenv.Dme
	HasLoadedEnvironment() bool

	HasActiveMap() bool

	HasUndo() bool
	HasRedo() bool

	PathsFilter() *dm.PathsFilter
}

type Menu struct {
	app app
}

func New(app app) *Menu {
	m := &Menu{app}
	m.addShortcuts()
	return m
}

func (m *Menu) Process() {
	w.MainMenuBar(w.Layout{
		w.Menu("File", w.Layout{
			w.MenuItem("Open Environment...", m.app.DoOpenEnvironment),
			w.Menu("Recent Environments", w.Layout{
				w.Custom(func() {
					for _, recentEnvironment := range m.app.RecentEnvironments() {
						w.MenuItem(recentEnvironment, func() {
							m.app.DoOpenEnvironmentByPath(recentEnvironment)
						}).Build()
					}
					w.Layout{
						w.Separator(),
						w.MenuItem("Clear Recent Environments", m.app.DoClearRecentEnvironments),
					}.Build()
				}),
			}).Enabled(len(m.app.RecentEnvironments()) != 0),
			w.Separator(),
			w.MenuItem("Open Map...", m.app.DoOpenMap).
				Enabled(m.app.HasLoadedEnvironment()).
				Shortcut("Ctrl+O"),
			w.Menu("Recent Maps", w.Layout{
				w.Custom(func() {
					for _, recentMap := range m.app.RecentMapsByLoadedEnvironment() {
						w.MenuItem(recentMap, func() {
							m.app.DoOpenMapByPath(recentMap)
						}).Build()
					}
					w.Layout{
						w.Separator(),
						w.MenuItem("Clear Recent Maps", m.app.DoClearRecentMaps),
					}.Build()
				}),
			}).Enabled(m.app.HasLoadedEnvironment() && len(m.app.RecentMapsByLoadedEnvironment()) != 0),
			w.Separator(),
			w.MenuItem("Save", m.app.DoSave).
				Enabled(m.app.HasActiveMap()).
				Shortcut("Ctrl+S"),
			w.Separator(),
			w.MenuItem("Preferences", m.app.DoOpenPreferences),
			w.Separator(),
			w.MenuItem("Exit", m.app.DoExit),
		}),

		w.Menu("Edit", w.Layout{
			w.MenuItem("Undo", m.app.DoUndo).
				Enabled(m.app.HasUndo()).
				Shortcut("Ctrl+Z"),
			w.MenuItem("Redo", m.app.DoRedo).
				Enabled(m.app.HasRedo()).
				Shortcut("Ctrl+Shift+Z"),
		}),

		w.Menu("Options", w.Layout{
			w.MenuItem("Toggle Area", m.doToggleTurf).
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isAreaToggled()).
				Shortcut("Ctrl+1"),
			w.MenuItem("Toggle Turf", m.doToggleTurf).
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isTurfToggled()).
				Shortcut("Ctrl+2"),
			w.MenuItem("Toggle Object", m.doToggleObject).
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isObjectToggled()).
				Shortcut("Ctrl+3"),
			w.MenuItem("Toggle Mob", m.doToggleMob).
				Enabled(m.app.HasLoadedEnvironment()).
				Selected(m.isMobToggled()).
				Shortcut("Ctrl+4"),
		}),

		w.Menu("Window", w.Layout{
			w.MenuItem("Reset Windows", m.app.DoResetWindows).Shortcut("F5"),
		}),

		w.Menu("Help", w.Layout{
			w.MenuItem("Open Logs Folder", m.app.DoOpenLogs),
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
