package tilemenu

import (
	"sdmm/internal/app/ui/shortcut"

	"github.com/go-gl/glfw/v3.3/glfw"
)

func (t *TileMenu) addShortcuts() {
	t.shortcuts.Add(shortcut.Shortcut{
		Name:      "tileMenu#close",
		FirstKey:  glfw.KeyEscape,
		Action:    t.close,
		IsEnabled: func() bool { return t.opened },
	})
}
