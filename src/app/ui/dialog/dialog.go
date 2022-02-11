package dialog

import (
	"github.com/SpaiR/imgui-go"
	"log"
)

type Type interface {
	Name() string
	Process()
}

var opened []Type

func Process() {
	var closedDialogs []Type
	for _, dialog := range opened {
		if !imgui.IsPopupOpen(dialog.Name()) {
			imgui.OpenPopup(dialog.Name())
		}

		if imgui.BeginPopupModalV(dialog.Name(), nil, imgui.WindowFlagsAlwaysAutoResize) {
			dialog.Process()
			imgui.EndPopup()
		}

		if !imgui.IsPopupOpen(dialog.Name()) {
			closedDialogs = append(closedDialogs, dialog)
		}
	}

	for _, dialog := range closedDialogs {
		Close(dialog)
	}
}

// Open opens the application dialog.
func Open(t Type) {
	log.Println("[dialog] opening dialog:", t.Name())
	opened = append(opened, t)
}

// Close closed the application dialog.
func Close(dialog Type) {
	log.Println("[dialog] closing dialog:", dialog.Name())
	for idx, t := range opened {
		if dialog.Name() == t.Name() {
			log.Println("[dialog] dialog closed:", dialog.Name())
			opened = append(opened[:idx], opened[idx+1:]...)
			return
		}
	}
}
