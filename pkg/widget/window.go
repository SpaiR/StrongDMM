package widget

import "github.com/SpaiR/imgui-go"

type windowWidget struct {
	id     string
	open   *bool
	flags  int
	layout Layout
}

func (w *windowWidget) Open(open *bool) *windowWidget {
	w.open = open
	return w
}

func (w *windowWidget) Flags(flags int) *windowWidget {
	w.flags = flags
	return w
}

func (w *windowWidget) Build() {
	if imgui.BeginV(w.id, w.open, w.flags) {
		w.layout.Build()
	}
	imgui.End()
}

func Window(id string, layout Layout) *windowWidget {
	return &windowWidget{
		id:     id,
		open:   nil,
		flags:  0,
		layout: layout,
	}
}
