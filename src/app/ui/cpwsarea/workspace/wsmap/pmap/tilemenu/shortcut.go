package tilemenu

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/shortcut"
)

func (t *TileMenu) addShortcuts() {
	t.shortcuts.Add(shortcut.Shortcut{
		Id:        "tileMenu#close",
		FirstKey:  glfw.KeyEscape,
		Action:    t.close,
		IsEnabled: func() bool { return t.opened },
	})
}
