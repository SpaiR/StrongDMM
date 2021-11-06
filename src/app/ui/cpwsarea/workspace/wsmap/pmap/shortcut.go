package pmap

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/shortcut"
)

func (p *PaneMap) addShortcuts() {
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#AddTool",
		FirstKey:    glfw.Key1,
		FirstKeyAlt: glfw.KeyKP1,
		Action:      p.selectAddTool,
	})
}
