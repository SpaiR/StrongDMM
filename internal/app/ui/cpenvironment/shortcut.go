package cpenvironment

import (
	"sdmm/internal/app/ui/shortcut"

	"github.com/go-gl/glfw/v3.3/glfw"
)

func (e *Environment) addShortcuts() {
	e.shortcuts.Add(shortcut.Shortcut{
		Name:     "cpenvironment#doToggleTypesFilter",
		FirstKey: glfw.KeyF,
		Action:   e.doToggleTypesFilter,
	})
}
