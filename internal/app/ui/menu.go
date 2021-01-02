package ui

import (
	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/internal/app/ui/shortcut"
	w "github.com/SpaiR/strongdmm/pkg/widget"
)

type menuAction interface {
	DoExit()
}

type Menu struct {
	action menuAction
}

func NewMenu(action menuAction) *Menu {
	addShortcuts(action)

	return &Menu{
		action: action,
	}
}

func (m *Menu) Process() {
	w.MainMenuBar(w.Layout{
		w.Menu("File", w.Layout{
			w.MenuItem("Open Environment...", nil),
			w.Separator(),
			w.MenuItem("Exit", m.action.DoExit).Shortcut("Ctrl+Q"),
		}),
	}).Build()
}

func addShortcuts(action menuAction) {
	shortcut.Add(shortcut.Shortcut{
		FirstKey:  glfw.KeyLeftControl,
		SecondKey: glfw.KeyQ,
		Action:    action.DoExit,
	})
	shortcut.Add(shortcut.Shortcut{
		FirstKey:  glfw.KeyRightControl,
		SecondKey: glfw.KeyQ,
		Action:    action.DoExit,
	})
}
