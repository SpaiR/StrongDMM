package menu

import (
	"sdmm/app/ui/shortcut"
	"sdmm/platform"

	"github.com/go-gl/glfw/v3.3/glfw"
)

func (m *Menu) addShortcuts() {
	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoNewWorkspace",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyN,
		Action:      m.app.DoNewWorkspace,
	})

	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoOpen",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyO,
		Action:      m.app.DoOpen,
	})

	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoSave",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyS,
		Action:      m.app.DoSave,
		IsEnabled:   m.app.HasActiveMap,
	})
	m.shortcuts.Add(shortcut.Shortcut{
		Name:         "menu#DoSaveAll",
		FirstKey:     platform.KeyModLeft(),
		FirstKeyAlt:  platform.KeyModRight(),
		SecondKey:    glfw.KeyLeftShift,
		SecondKeyAlt: glfw.KeyRightShift,
		ThirdKey:     glfw.KeyS,
		Action:       m.app.DoSaveAll,
		IsEnabled:    m.app.HasActiveMap,
	})

	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoClose",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyW,
		Action:      m.app.DoClose,
	})
	m.shortcuts.Add(shortcut.Shortcut{
		Name:         "menu#DoCloseAll",
		FirstKey:     platform.KeyModLeft(),
		FirstKeyAlt:  platform.KeyModRight(),
		SecondKey:    glfw.KeyLeftShift,
		SecondKeyAlt: glfw.KeyRightShift,
		ThirdKey:     glfw.KeyW,
		Action:       m.app.DoCloseAll,
	})
	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoExit",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyQ,
		Action:      m.app.DoExit,
	})

	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoUndo",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyZ,
		Action:      m.app.DoUndo,
		IsEnabled:   m.app.CommandStorage().HasUndo,
	})

	m.shortcuts.Add(shortcut.Shortcut{
		Name:         "menu#DoRedo",
		FirstKey:     platform.KeyModLeft(),
		FirstKeyAlt:  platform.KeyModRight(),
		SecondKey:    glfw.KeyLeftShift,
		SecondKeyAlt: glfw.KeyRightShift,
		ThirdKey:     glfw.KeyZ,
		Action:       m.app.DoRedo,
		IsEnabled:    m.app.CommandStorage().HasRedo,
	})
	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoRedo",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyY,
		Action:      m.app.DoRedo,
		IsEnabled:   m.app.CommandStorage().HasRedo,
	})

	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoCopy",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyC,
		Action:      m.app.DoCopy,
	})
	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoPaste",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyV,
		Action:      m.app.DoPaste,
		IsEnabled:   m.app.Clipboard().HasData,
	})
	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoCut",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyX,
		Action:      m.app.DoCut,
	})
	m.shortcuts.Add(shortcut.Shortcut{
		Name:     "menu#DoDelete",
		FirstKey: glfw.KeyDelete,
		Action:   m.app.DoDelete,
	})
	m.shortcuts.Add(shortcut.Shortcut{
		Name:        "menu#DoSearch",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyF,
		Action:      m.app.DoSearch,
		IsEnabled:   m.app.HasActiveMap,
	})

	m.shortcuts.Add(shortcut.Shortcut{
		Name:         "menu#DoMultiZRendering",
		FirstKey:     platform.KeyModLeft(),
		FirstKeyAlt:  platform.KeyModRight(),
		SecondKey:    glfw.Key0,
		SecondKeyAlt: glfw.KeyKP0,
		Action:       m.app.DoMultiZRendering,
	})

	m.shortcuts.Add(shortcut.Shortcut{
		Name:     "menu#DoResetLayout",
		FirstKey: glfw.KeyF5,
		Action:   m.app.DoResetLayout,
	})

	m.shortcuts.SetVisible(true)
}
