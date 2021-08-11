package ui

import (
	"github.com/SpaiR/strongdmm/app/ui/shortcut"
	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/pkg/dm/dmenv"
	w "github.com/SpaiR/strongdmm/pkg/imguiext/widget"
)

//goland:noinspection GoCommentStart
type menuAction interface {
	// File
	AppDoOpenEnvironment()
	AppDoOpenEnvironmentByPath(path string)
	AppDoClearRecentEnvironments()
	AppDoOpenMap()
	AppDoOpenMapByPath(path string)
	AppDoClearRecentMaps()
	AppDoExit()

	// Edit
	AppDoUndo()
	AppDoRedo()

	// Window
	AppDoResetWindows()

	// Help
	AppDoOpenLogs()

	AppRecentEnvironments() []string
	AppRecentMapsByLoadedEnvironment() []string

	AppLoadedEnvironment() *dmenv.Dme
	AppHasLoadedEnvironment() bool

	AppHasUndo() bool
	AppHasRedo() bool
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
			w.MenuItem("Open Map...", m.action.AppDoOpenMap).Enabled(m.action.AppHasLoadedEnvironment()),
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
			w.MenuItem("Exit", m.action.AppDoExit),
		}),

		w.Menu("Edit", w.Layout{
			w.MenuItem("Undo", m.action.AppDoUndo).Enabled(m.action.AppHasUndo()),
			w.MenuItem("Redo", m.action.AppDoRedo).Enabled(m.action.AppHasRedo()),
		}),

		w.Menu("Window", w.Layout{
			w.MenuItem("Reset Windows", m.action.AppDoResetWindows).Shortcut("F5"),
		}),

		w.Menu("Help", w.Layout{
			w.MenuItem("Open Logs Folder", m.action.AppDoOpenLogs),
		}),
	}).Build()
}

func addShortcuts(action menuAction) {
	shortcut.Add(shortcut.Shortcut{
		FirstKey: glfw.KeyF5,
		Action:   action.AppDoResetWindows,
	})
}
