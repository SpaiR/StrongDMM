package dialog

import (
	w "sdmm/imguiext/widget"
)

type TypeCustom struct {
	Title       string
	Layout      w.Layout
	CloseButton bool
}

func (t TypeCustom) Name() string {
	return t.Title
}

func (t TypeCustom) Process() {
	t.Layout.Build()
}

func (t TypeCustom) HasCloseButton() bool {
	return t.CloseButton
}
