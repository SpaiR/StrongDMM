package cpvareditor

import (
	"sdmm/app/ui/shortcut"
	"sdmm/platform"

	"github.com/go-gl/glfw/v3.3/glfw"
)

func (v *VarEditor) addShortcuts() {
	v.shortcuts.Add(shortcut.Shortcut{
		Name:         "cpvareditor#doToggleShowModified",
		FirstKey:     platform.KeyModLeft(),
		FirstKeyAlt:  platform.KeyModRight(),
		SecondKey:    glfw.Key1,
		SecondKeyAlt: glfw.KeyKP1,
		Action:       v.doToggleShowModified,
	})
	v.shortcuts.Add(shortcut.Shortcut{
		Name:         "cpvareditor#doToggleShowByType",
		FirstKey:     platform.KeyModLeft(),
		FirstKeyAlt:  platform.KeyModRight(),
		SecondKey:    glfw.Key2,
		SecondKeyAlt: glfw.KeyKP2,
		Action:       v.doToggleShowByType,
	})
	v.shortcuts.Add(shortcut.Shortcut{
		Name:         "cpvareditor#doToggleShowPins",
		FirstKey:     platform.KeyModLeft(),
		FirstKeyAlt:  platform.KeyModRight(),
		SecondKey:    glfw.Key3,
		SecondKeyAlt: glfw.KeyKP3,
		Action:       v.doToggleShowPins,
	})
	v.shortcuts.Add(shortcut.Shortcut{
		Name:         "cpvareditor#doToggleShowTmp",
		FirstKey:     platform.KeyModLeft(),
		FirstKeyAlt:  platform.KeyModRight(),
		SecondKey:    glfw.Key4,
		SecondKeyAlt: glfw.KeyKP4,
		Action:       v.doToggleShowTmp,
	})
}
