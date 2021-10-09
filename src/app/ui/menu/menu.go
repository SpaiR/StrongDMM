package menu

import (
	"sdmm/dm"
	"sdmm/dm/dmenv"
	w "sdmm/imguiext/widget"
)

//goland:noinspection GoCommentStart
type action interface {
	// File
	AppDoOpenEnvironment()
	AppDoOpenEnvironmentByPath(path string)
	AppDoClearRecentEnvironments()
	AppDoOpenMap() // Ctrl+O
	AppDoOpenMapByPath(path string)
	AppDoClearRecentMaps()
	AppDoSave() // Ctrl+S
	AppDoOpenPreferences()
	AppDoExit()

	// Edit
	AppDoUndo() // Ctrl+Z
	AppDoRedo() // Ctrl+Shift+Z | Ctrl+Y

	// Window
	AppDoResetWindows() // F5

	// Help
	AppDoOpenLogs()

	// Helpers

	AppRecentEnvironments() []string
	AppRecentMapsByLoadedEnvironment() []string

	AppLoadedEnvironment() *dmenv.Dme
	AppHasLoadedEnvironment() bool

	AppHasActiveMap() bool

	AppHasUndo() bool
	AppHasRedo() bool

	AppPathsFilter() *dm.PathsFilter
}

type Menu struct {
	action action
}

func New(action action) *Menu {
	m := &Menu{action}
	m.addShortcuts()
	return m
}

func (m *Menu) Process() {
	w.MainMenuBar(w.Layout{
		w.Menu("File", w.Layout{
			w.MenuItem("Open Environment...", m.action.AppDoOpenEnvironment),
			w.Menu("Recent Environments", w.Layout{
				w.Custom(func() {
					for _, recentEnvironment := range m.action.AppRecentEnvironments() {
						w.MenuItem(recentEnvironment, func() {
							m.action.AppDoOpenEnvironmentByPath(recentEnvironment)
						}).Build()
					}
					w.Layout{
						w.Separator(),
						w.MenuItem("Clear Recent Environments", m.action.AppDoClearRecentEnvironments),
					}.Build()
				}),
			}).Enabled(len(m.action.AppRecentEnvironments()) != 0),
			w.Separator(),
			w.MenuItem("Open Map...", m.action.AppDoOpenMap).
				Enabled(m.action.AppHasLoadedEnvironment()).
				Shortcut("Ctrl+O"),
			w.Menu("Recent Maps", w.Layout{
				w.Custom(func() {
					for _, recentMap := range m.action.AppRecentMapsByLoadedEnvironment() {
						w.MenuItem(recentMap, func() {
							m.action.AppDoOpenMapByPath(recentMap)
						}).Build()
					}
					w.Layout{
						w.Separator(),
						w.MenuItem("Clear Recent Maps", m.action.AppDoClearRecentMaps),
					}.Build()
				}),
			}).Enabled(m.action.AppHasLoadedEnvironment() && len(m.action.AppRecentMapsByLoadedEnvironment()) != 0),
			w.Separator(),
			w.MenuItem("Save", m.action.AppDoSave).
				Enabled(m.action.AppHasActiveMap()).
				Shortcut("Ctrl+S"),
			w.Separator(),
			w.MenuItem("Preferences", m.action.AppDoOpenPreferences),
			w.Separator(),
			w.MenuItem("Exit", m.action.AppDoExit),
		}),

		w.Menu("Edit", w.Layout{
			w.MenuItem("Undo", m.action.AppDoUndo).
				Enabled(m.action.AppHasUndo()).
				Shortcut("Ctrl+Z"),
			w.MenuItem("Redo", m.action.AppDoRedo).
				Enabled(m.action.AppHasRedo()).
				Shortcut("Ctrl+Shift+Z"),
		}),

		w.Menu("Options", w.Layout{
			w.MenuItem("Toggle Area", m.doToggleTurf).
				Enabled(m.action.AppHasLoadedEnvironment()).
				Selected(m.isAreaToggled()).
				Shortcut("Ctrl+1"),
			w.MenuItem("Toggle Turf", m.doToggleTurf).
				Enabled(m.action.AppHasLoadedEnvironment()).
				Selected(m.isTurfToggled()).
				Shortcut("Ctrl+2"),
			w.MenuItem("Toggle Object", m.doToggleObject).
				Enabled(m.action.AppHasLoadedEnvironment()).
				Selected(m.isObjectToggled()).
				Shortcut("Ctrl+3"),
			w.MenuItem("Toggle Mob", m.doToggleMob).
				Enabled(m.action.AppHasLoadedEnvironment()).
				Selected(m.isMobToggled()).
				Shortcut("Ctrl+4"),
		}),

		w.Menu("Window", w.Layout{
			w.MenuItem("Reset Windows", m.action.AppDoResetWindows).Shortcut("F5"),
		}),

		w.Menu("Help", w.Layout{
			w.MenuItem("Open Logs Folder", m.action.AppDoOpenLogs),
		}),
	}).Build()
}

func (m *Menu) doToggleArea() {
	m.action.AppPathsFilter().TogglePath("/area")
}

func (m *Menu) doToggleTurf() {
	m.action.AppPathsFilter().TogglePath("/turf")
}

func (m *Menu) doToggleObject() {
	m.action.AppPathsFilter().TogglePath("/obj")
}

func (m *Menu) doToggleMob() {
	m.action.AppPathsFilter().TogglePath("/mob")
}

func (m *Menu) isAreaToggled() bool {
	return m.action.AppPathsFilter().IsVisiblePath("/area")
}

func (m *Menu) isTurfToggled() bool {
	return m.action.AppPathsFilter().IsVisiblePath("/turf")
}

func (m *Menu) isObjectToggled() bool {
	return m.action.AppPathsFilter().IsVisiblePath("/obj")
}

func (m *Menu) isMobToggled() bool {
	return m.action.AppPathsFilter().IsVisiblePath("/mob")
}
