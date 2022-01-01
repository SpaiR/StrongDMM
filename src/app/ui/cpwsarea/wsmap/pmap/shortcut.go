package pmap

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/app/ui/shortcut"
)

func (p *PaneMap) addShortcuts() {
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#selectAddTool",
		FirstKey:    glfw.Key1,
		FirstKeyAlt: glfw.KeyKP1,
		Action:      p.selectAddTool,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#selectFillTool",
		FirstKey:    glfw.Key2,
		FirstKeyAlt: glfw.KeyKP2,
		Action:      p.selectFillTool,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#selectSelectTool",
		FirstKey:    glfw.Key3,
		FirstKeyAlt: glfw.KeyKP3,
		Action:      p.selectSelectTool,
	})

	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#doDeselectAll",
		FirstKey:    shortcut.KeyLeftCmd(),
		FirstKeyAlt: shortcut.KeyRightCmd(),
		SecondKey:   glfw.KeyD,
		Action:      p.DoDeselect,
	})

	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleArea",
		FirstKey:     shortcut.KeyLeftCmd(),
		FirstKeyAlt:  shortcut.KeyRightCmd(),
		SecondKey:    glfw.Key1,
		SecondKeyAlt: glfw.KeyKP1,
		Action:       p.doToggleArea,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleTurf",
		FirstKey:     shortcut.KeyLeftCmd(),
		FirstKeyAlt:  shortcut.KeyRightCmd(),
		SecondKey:    glfw.Key2,
		SecondKeyAlt: glfw.KeyKP2,
		Action:       p.doToggleTurf,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleObject",
		FirstKey:     shortcut.KeyLeftCmd(),
		FirstKeyAlt:  shortcut.KeyRightCmd(),
		SecondKey:    glfw.Key3,
		SecondKeyAlt: glfw.KeyKP3,
		Action:       p.doToggleObject,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleMob",
		FirstKey:     shortcut.KeyLeftCmd(),
		FirstKeyAlt:  shortcut.KeyRightCmd(),
		SecondKey:    glfw.Key4,
		SecondKeyAlt: glfw.KeyKP4,
		Action:       p.doToggleMob,
	})

	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#doPreviousLevel",
		FirstKey:    shortcut.KeyLeftCmd(),
		FirstKeyAlt: shortcut.KeyRightCmd(),
		SecondKey:   glfw.KeyDown,
		Action:      p.doPreviousLevel,
		IsEnabled:   p.hasPreviousLevel,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#doNextLevel",
		FirstKey:    shortcut.KeyLeftCmd(),
		FirstKeyAlt: shortcut.KeyRightCmd(),
		SecondKey:   glfw.KeyUp,
		Action:      p.doNextLevel,
		IsEnabled:   p.hasNextLevel,
	})
}

func (p *PaneMap) doToggleArea() {
	p.app.PathsFilter().TogglePath("/area")
}

func (p *PaneMap) doToggleTurf() {
	p.app.PathsFilter().TogglePath("/turf")
}

func (p *PaneMap) doToggleObject() {
	p.app.PathsFilter().TogglePath("/obj")
}

func (p *PaneMap) doToggleMob() {
	p.app.PathsFilter().TogglePath("/mob")
}

func (p *PaneMap) DoDeselect() {
	tools.Tools()[tools.TNGrab].OnDeselect()
}
