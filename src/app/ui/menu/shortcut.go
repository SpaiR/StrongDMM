package menu

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/shortcut"
)

func (m *Menu) addShortcuts() {
	shortcut.Add(shortcut.Shortcut{
		Name:      "AppDoSave",
		FirstKey:  glfw.KeyLeftControl,
		SecondKey: glfw.KeyS,
		Action:    m.action.AppDoSave,
		IsEnabled: m.action.AppHasActiveMap,
	})
	shortcut.Add(shortcut.Shortcut{
		Name:      "AppDoSave",
		FirstKey:  glfw.KeyRightControl,
		SecondKey: glfw.KeyS,
		Action:    m.action.AppDoSave,
		IsEnabled: m.action.AppHasActiveMap,
	})

	shortcut.Add(shortcut.Shortcut{
		Name:     "AppDoResetWindows",
		FirstKey: glfw.KeyF5,
		Action:   m.action.AppDoResetWindows,
	})
}
