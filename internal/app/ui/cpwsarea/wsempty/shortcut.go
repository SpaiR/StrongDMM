package wsempty

import (
	"sdmm/internal/app/ui/shortcut"
	"sdmm/internal/platform"

	"github.com/go-gl/glfw/v3.3/glfw"
)

func (ws *WsEmpty) addShortcuts() {
	ws.shortcuts.Add(shortcut.Shortcut{
		Name:        "wsempty#loadSelectedMaps",
		FirstKey:    glfw.KeyEnter,
		FirstKeyAlt: glfw.KeyKPEnter,
		Action:      ws.loadSelectedMaps,
	})

	ws.shortcuts.Add(shortcut.Shortcut{
		Name:     "wsempty#dropSelectedMaps",
		FirstKey: glfw.KeyEscape,
		Action:   ws.dropSelectedMaps,
	})
	ws.shortcuts.Add(shortcut.Shortcut{
		Name:        "wsempty#dropSelectedMaps",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyD,
		Action:      ws.dropSelectedMaps,
	})

	ws.shortcuts.Add(shortcut.Shortcut{
		Name:        "wsempty#selectAllMaps",
		FirstKey:    platform.KeyModLeft(),
		FirstKeyAlt: platform.KeyModRight(),
		SecondKey:   glfw.KeyA,
		Action:      ws.selectAllMaps,
	})
}
