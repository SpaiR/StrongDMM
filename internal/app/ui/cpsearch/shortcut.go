package cpsearch

import (
	"sdmm/internal/app/ui/shortcut"

	"github.com/go-gl/glfw/v3.3/glfw"
)

func (s *Search) addShortcuts() {
	s.shortcuts.Add(shortcut.Shortcut{
		Name:        "cpsearch#jumpToUp",
		FirstKey:    glfw.KeyLeftShift,
		FirstKeyAlt: glfw.KeyRightShift,
		SecondKey:   glfw.KeyF3,
		Action:      s.jumpToUp,
	})
	s.shortcuts.Add(shortcut.Shortcut{
		Name:     "cpsearch#jumpToDown",
		FirstKey: glfw.KeyF3,
		Action:   s.jumpToDown,
	})
	s.shortcuts.Add(shortcut.Shortcut{
		Name:     "cpseaarch#doToggleFilter",
		FirstKey: glfw.KeyF,
		Action:   s.doToggleFilter,
	})
}
