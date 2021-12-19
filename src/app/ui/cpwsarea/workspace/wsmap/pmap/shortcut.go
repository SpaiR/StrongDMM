package pmap

import (
	"github.com/go-gl/glfw/v3.3/glfw"
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
		Name:         "pmap#doToggleArea",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key1,
		SecondKeyAlt: glfw.KeyKP1,
		Action:       p.doToggleArea,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleTurf",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key2,
		SecondKeyAlt: glfw.KeyKP2,
		Action:       p.doToggleTurf,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleObject",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key3,
		SecondKeyAlt: glfw.KeyKP3,
		Action:       p.doToggleObject,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleMob",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key4,
		SecondKeyAlt: glfw.KeyKP4,
		Action:       p.doToggleMob,
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
