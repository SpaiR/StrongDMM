package dialog

import (
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"

	"github.com/SpaiR/imgui-go"
)

type TypeConfirmation struct {
	Title        string
	Question     string
	ActionYes    func()
	ActionNo     func()
	ActionCancel func()
}

func (t TypeConfirmation) Name() string {
	return t.Title
}

func (TypeConfirmation) HasCloseButton() bool {
	return false
}

func (t TypeConfirmation) Process() {
	w.Layout{
		w.Text(t.Question),
		w.Separator(),
		w.Button("Yes", t.doYes).
			Style(style.ButtonGreen{}),
		w.SameLine(),
		w.Button("No", t.doNo).
			Style(style.ButtonRed{}),
		w.Custom(func() {
			if t.ActionCancel != nil {
				w.Layout{
					w.SameLine(),
					w.Button("Cancel", t.doCancel),
				}.Build()
			}
		}),
	}.Build()
}

func (t TypeConfirmation) doYes() {
	if t.ActionYes != nil {
		t.ActionYes()
	}
	imgui.CloseCurrentPopup()
}

func (t TypeConfirmation) doNo() {
	if t.ActionNo != nil {
		t.ActionNo()
	}
	imgui.CloseCurrentPopup()
}

func (t TypeConfirmation) doCancel() {
	if t.ActionCancel != nil {
		t.ActionCancel()
	}
	imgui.CloseCurrentPopup()
}
