package ui

import (
	shortcut2 "github.com/SpaiR/strongdmm/app/ui/shortcut"
	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/pkg/dm/dmenv"
	w "github.com/SpaiR/strongdmm/pkg/imguiext/widget"
)

type menuAction interface {
	// File
	DoOpenEnvironment()
	DoOpenEnvironmentByPath(path string)
	DoClearRecentEnvironments()
	DoOpenMap()
	DoOpenMapByPath(path string)
	DoClearRecentMaps()
	DoExit()

	// Window
	DoResetWindows()

	// Help
	DoOpenLogs()

	RecentEnvironments() []string
	RecentMapsByLoadedEnvironment() []string

	LoadedEnvironment() *dmenv.Dme
	HasLoadedEnvironment() bool
}

type Menu struct {
	action menuAction
}

func NewMenu(action menuAction) *Menu {
	addShortcuts(action)

	return &Menu{
		action: action,
	}
}

func (m *Menu) Process() {
	w.MainMenuBar(w.Layout{
		w.Menu("File", w.Layout{
			w.MenuItem("Open Environment...", m.action.DoOpenEnvironment),
			w.Menu("Recent Environments", w.Layout{
				w.Custom(func() {
					for _, recentEnvironment := range m.action.RecentEnvironments() {
						w.MenuItem(recentEnvironment, func() {
							m.action.DoOpenEnvironmentByPath(recentEnvironment)
						}).Build()
					}
					w.Layout{
						w.Separator(),
						w.MenuItem("Clear Recent Environments", m.action.DoClearRecentEnvironments),
					}.Build()
				}),
			}).Enabled(len(m.action.RecentEnvironments()) != 0),
			w.Separator(),
			w.MenuItem("Open Map...", m.action.DoOpenMap).Enabled(m.action.HasLoadedEnvironment()),
			w.Menu("Recent Maps", w.Layout{
				w.Custom(func() {
					for _, recentMap := range m.action.RecentMapsByLoadedEnvironment() {
						w.MenuItem(recentMap, func() {
							m.action.DoOpenMapByPath(recentMap)
						}).Build()
					}
					w.Layout{
						w.Separator(),
						w.MenuItem("Clear Recent Maps", m.action.DoClearRecentMaps),
					}.Build()
				}),
			}).Enabled(m.action.HasLoadedEnvironment() && len(m.action.RecentMapsByLoadedEnvironment()) != 0),
			w.Separator(),
			w.MenuItem("Exit", m.action.DoExit),
		}),

		w.Menu("Window", w.Layout{
			w.MenuItem("Reset Windows", m.action.DoResetWindows).Shortcut("F5"),
		}),
		w.Menu("Help", w.Layout{
			w.MenuItem("Open Logs Folder", m.action.DoOpenLogs),
		}),
	}).Build()
}

func addShortcuts(action menuAction) {
	shortcut2.Add(shortcut2.Shortcut{
		FirstKey: glfw.KeyF5,
		Action:   action.DoResetWindows,
	})
}
