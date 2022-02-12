package dialog

import "github.com/SpaiR/imgui-go"

type TypeSimple struct {
	Title   string
	Message string
}

func (t TypeSimple) Name() string {
	return t.Title
}

func (t TypeSimple) Process() {
	imgui.Text(t.Message)
}

func (t TypeSimple) HasCloseButton() bool {
	return true
}
