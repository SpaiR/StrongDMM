package ui

import (
	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/internal/app/ui/shortcut"
	w "github.com/SpaiR/strongdmm/pkg/widget"
)

type menuAction interface {
	// File
	DoOpenEnvironment()
	DoOpenEnvironmentByPath(path string)
	DoOpenMap()
	DoExit()

	// Window
	DoResetWindows()

	// Help
	DoOpenLogs()

	RecentEnvironments() []string
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
				}),
			}).Enabled(len(m.action.RecentEnvironments()) != 0),
			w.Separator(),
			w.MenuItem("Open Map...", m.action.DoOpenMap),
			w.Separator(),
			w.MenuItem("Exit", m.action.DoExit).Shortcut("Ctrl+Q"),
		}),

		w.Menu("Window", w.Layout{
			w.MenuItem("Reset Windows", m.action.DoResetWindows).Shortcut("F5"),
		}),

		w.Menu("Help", w.Layout{
			w.MenuItem("Logs...", m.action.DoOpenLogs),
		}),
	}).Build()
}

func addShortcuts(action menuAction) {
	shortcut.Add(shortcut.Shortcut{
		FirstKey:  glfw.KeyLeftControl,
		SecondKey: glfw.KeyQ,
		Action:    action.DoExit,
	})
	shortcut.Add(shortcut.Shortcut{
		FirstKey:  glfw.KeyRightControl,
		SecondKey: glfw.KeyQ,
		Action:    action.DoExit,
	})

	shortcut.Add(shortcut.Shortcut{
		FirstKey: glfw.KeyF5,
		Action:   action.DoResetWindows,
	})
}
