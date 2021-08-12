package ui

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"strongdmm/app/ui/shortcut"
	"strongdmm/dm/dmenv"
	"strongdmm/imguiext/widget"
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
	widget.MainMenuBar(widget.Layout{
		widget.Menu("File", widget.Layout{
			widget.MenuItem("Open Environment...", m.action.AppDoOpenEnvironment),
			widget.Menu("Recent Environments", widget.Layout{
				widget.Custom(func() {
					for _, recentEnvironment := range m.action.AppRecentEnvironments() {
						widget.MenuItem(recentEnvironment, func() {
							m.action.AppDoOpenEnvironmentByPath(recentEnvironment)
						}).Build()
					}
					widget.Layout{
						widget.Separator(),
						widget.MenuItem("Clear Recent Environments", m.action.AppDoClearRecentEnvironments),
					}.Build()
				}),
			}).Enabled(len(m.action.AppRecentEnvironments()) != 0),
			widget.Separator(),
			widget.MenuItem("Open Map...", m.action.AppDoOpenMap).Enabled(m.action.AppHasLoadedEnvironment()),
			widget.Menu("Recent Maps", widget.Layout{
				widget.Custom(func() {
					for _, recentMap := range m.action.AppRecentMapsByLoadedEnvironment() {
						widget.MenuItem(recentMap, func() {
							m.action.AppDoOpenMapByPath(recentMap)
						}).Build()
					}
					widget.Layout{
						widget.Separator(),
						widget.MenuItem("Clear Recent Maps", m.action.AppDoClearRecentMaps),
					}.Build()
				}),
			}).Enabled(m.action.AppHasLoadedEnvironment() && len(m.action.AppRecentMapsByLoadedEnvironment()) != 0),
			widget.Separator(),
			widget.MenuItem("Exit", m.action.AppDoExit),
		}),

		widget.Menu("Edit", widget.Layout{
			widget.MenuItem("Undo", m.action.AppDoUndo).Enabled(m.action.AppHasUndo()),
			widget.MenuItem("Redo", m.action.AppDoRedo).Enabled(m.action.AppHasRedo()),
		}),

		widget.Menu("Window", widget.Layout{
			widget.MenuItem("Reset Windows", m.action.AppDoResetWindows).Shortcut("F5"),
		}),

		widget.Menu("Help", widget.Layout{
			widget.MenuItem("Open Logs Folder", m.action.AppDoOpenLogs),
		}),
	}).Build()
}

func addShortcuts(action menuAction) {
	shortcut.Add(shortcut.Shortcut{
		FirstKey: glfw.KeyF5,
		Action:   action.AppDoResetWindows,
	})
}
