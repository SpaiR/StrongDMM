package cpvareditor

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/shortcut"
)

func (v *VarEditor) addShortcuts() {
	v.shortcuts.Add(shortcut.Shortcut{
		Name:         "cpvareditor#doToggleShowModified",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key1,
		SecondKeyAlt: glfw.KeyKP1,
		Action:       v.doToggleShowModified,
	})
	v.shortcuts.Add(shortcut.Shortcut{
		Name:         "cpvareditor#doToggleShowByType",
		FirstKey:     glfw.KeyLeftControl,
		FirstKeyAlt:  glfw.KeyRightControl,
		SecondKey:    glfw.Key2,
		SecondKeyAlt: glfw.KeyKP2,
		Action:       v.doToggleShowByType,
	})
}
