package cpenvironment

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/shortcut"
)

func (e *Environment) addShortcuts() {
	e.shortcuts.Add(shortcut.Shortcut{
		Name:     "cpenvironment#doToggleTypesFilter",
		FirstKey: glfw.KeyF,
		Action:   e.doToggleTypesFilter,
	})
}
