package menu

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/shortcut"
)

func (m *Menu) addShortcuts() {
	shortcut.Add(shortcut.Shortcut{
		Name:        "AppDoOpenMap",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyO,
		Action:      m.action.AppDoOpenMap,
		IsEnabled:   m.action.AppHasLoadedEnvironment,
	})

	shortcut.Add(shortcut.Shortcut{
		Name:        "AppDoSave",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyS,
		Action:      m.action.AppDoSave,
		IsEnabled:   m.action.AppHasActiveMap,
	})

	shortcut.Add(shortcut.Shortcut{
		Name:        "AppDoUndo",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyZ,
		Action:      m.action.AppDoUndo,
		IsEnabled:   m.action.AppHasUndo,
	})

	shortcut.Add(shortcut.Shortcut{
		Name:         "AppDoRedo",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.KeyLeftShift,
		SecondKeyAlt: glfw.KeyRightShift,
		ThirdKey:     glfw.KeyZ,
		Action:       m.action.AppDoRedo,
		IsEnabled:    m.action.AppHasRedo,
	})
	shortcut.Add(shortcut.Shortcut{
		Name:        "AppDoRedo",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyY,
		Action:      m.action.AppDoRedo,
		IsEnabled:   m.action.AppHasRedo,
	})

	shortcut.Add(shortcut.Shortcut{
		Name:     "AppDoResetWindows",
		FirstKey: glfw.KeyF5,
		Action:   m.action.AppDoResetWindows,
	})
}
