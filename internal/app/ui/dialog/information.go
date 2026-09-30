package dialog

import (
	"sdmm/internal/platform"

	"github.com/SpaiR/imgui-go"
)

type TypeInformation struct {
	Title       string
	Information string
	AllowCopy   bool
}

func (t TypeInformation) Name() string {
	return t.Title
}

func (TypeInformation) HasCloseButton() bool {
	return false
}

func (t TypeInformation) Process() {
	//*
	imgui.Text(t.Information)
	imgui.Separator()
	if imgui.Button("OK") {
		imgui.CloseCurrentPopup()
	}
	if t.AllowCopy {
		if imgui.Button("Copy") {
			platform.SetClipboard(t.Information)
		}
	}
}
