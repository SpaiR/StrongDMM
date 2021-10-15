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
		Action:      m.app.DoOpenMap,
		IsEnabled:   m.app.HasLoadedEnvironment,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#AppDoSave",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyS,
		Action:      m.app.DoSave,
		IsEnabled:   m.app.HasActiveMap,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#AppDoUndo",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyZ,
		Action:      m.app.DoUndo,
		IsEnabled:   m.app.CommandStorage().HasUndo,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#AppDoRedo",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.KeyLeftShift,
		SecondKeyAlt: glfw.KeyRightShift,
		ThirdKey:     glfw.KeyZ,
		Action:       m.app.DoRedo,
		IsEnabled:    m.app.CommandStorage().HasRedo,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#AppDoRedo",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyY,
		Action:      m.app.DoRedo,
		IsEnabled:   m.app.CommandStorage().HasRedo,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#DoCopy",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyC,
		Action:      m.app.DoCopy,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#DoPaste",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyV,
		Action:      m.app.DoPaste,
		IsEnabled:   m.app.Clipboard().HasData,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:          "menu#DoCut",
		FirstKey:    glfw.KeyLeftControl,
		FirstKeyAlt: glfw.KeyRightControl,
		SecondKey:   glfw.KeyX,
		Action:      m.app.DoCut,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:       "menu#DoDelete",
		FirstKey: glfw.KeyDelete,
		Action:   m.app.DoDelete,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#doToggleArea",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key1,
		SecondKeyAlt: glfw.KeyKP1,
		Action:       m.doToggleArea,
		IsEnabled:    m.app.HasLoadedEnvironment,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#doToggleTurf",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key2,
		SecondKeyAlt: glfw.KeyKP2,
		Action:       m.doToggleTurf,
		IsEnabled:    m.app.HasLoadedEnvironment,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#doToggleObject",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key3,
		SecondKeyAlt: glfw.KeyKP3,
		Action:       m.doToggleObject,
		IsEnabled:    m.app.HasLoadedEnvironment,
	})
	shortcut.Add(shortcut.Shortcut{
		Id:           "menu#doToggleMob",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key4,
		SecondKeyAlt: glfw.KeyKP4,
		Action:       m.doToggleMob,
		IsEnabled:    m.app.HasLoadedEnvironment,
	})

	shortcut.Add(shortcut.Shortcut{
		Id:       "menu#AppDoResetWindows",
		FirstKey: glfw.KeyF5,
		Action:   m.app.DoResetWindows,
	})
}
