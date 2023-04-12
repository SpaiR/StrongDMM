package dialog

import (
	"github.com/SpaiR/imgui-go"
)

type TypeInformation struct {
	Title       string
	Information string
}

func (t TypeInformation) Name() string {
	return t.Title
}

func (TypeInformation) HasCloseButton() bool {
	return false
}

func (t TypeInformation) Process() {
	imgui.Text(t.Information)
	imgui.Separator()
	if imgui.Button("OK") {
		imgui.CloseCurrentPopup()
	}
}
