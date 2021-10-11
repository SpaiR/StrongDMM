package menu

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/shortcut"
)

func (m *Menu) addShortcuts() {
	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#AppDoOpenMap",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyO,
		Action:      m.action.AppDoOpenMap,
		IsEnabled:   m.action.AppHasLoadedEnvironment,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#AppDoSave",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyS,
		Action:      m.action.AppDoSave,
		IsEnabled:   m.action.AppHasActiveMap,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#AppDoUndo",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyZ,
		Action:      m.action.AppDoUndo,
		IsEnabled:   m.action.AppHasUndo,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#AppDoRedo",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.KeyLeftShift,
		SecondKeyAlt: glfw.KeyRightShift,
		ThirdKey:     glfw.KeyZ,
		Action:       m.action.AppDoRedo,
		IsEnabled:    m.action.AppHasRedo,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#AppDoRedo",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyY,
		Action:      m.action.AppDoRedo,
		IsEnabled:   m.action.AppHasRedo,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#doToggleArea",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key1,
		SecondKeyAlt: glfw.KeyKP1,
		Action:       m.doToggleArea,
		IsEnabled:    m.action.AppHasLoadedEnvironment,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#doToggleTurf",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key2,
		SecondKeyAlt: glfw.KeyKP2,
		Action:       m.doToggleTurf,
		IsEnabled:    m.action.AppHasLoadedEnvironment,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#doToggleObject",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key3,
		SecondKeyAlt: glfw.KeyKP3,
		Action:       m.doToggleObject,
		IsEnabled:    m.action.AppHasLoadedEnvironment,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#doToggleMob",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key4,
		SecondKeyAlt: glfw.KeyKP4,
		Action:       m.doToggleMob,
		IsEnabled:    m.action.AppHasLoadedEnvironment,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:       "menu#AppDoResetWindows",
		FirstKey: glfw.KeyF5,
		Action:   m.action.AppDoResetWindows,
	})
}
