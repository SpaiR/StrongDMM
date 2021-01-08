package widget

import "github.com/SpaiR/imgui-go"

type windowWidget struct {
	id     string
	open   *bool
	flags  int
	layout Layout
	push   func()
	pop    func()
}

func (w *windowWidget) Open(open *bool) *windowWidget {
	w.open = open
	return w
}

func (w *windowWidget) Flags(flags int) *windowWidget {
	w.flags = flags
	return w
}

func (w *windowWidget) Push(push func()) *windowWidget {
	w.push = push
	return w
}

func (w *windowWidget) Pop(pop func()) *windowWidget {
	w.pop = pop
	return w
}

func (w *windowWidget) Build() {
	if w.push != nil {
		w.push()
	}
	if imgui.BeginV(w.id, w.open, w.flags) {
		if w.pop != nil {
			w.pop()
		}
		w.layout.Build()
	} else if w.pop != nil {
		w.pop()
	}
	imgui.End()
}

func Window(id string, layout Layout) *windowWidget {
	return &windowWidget{
		id:     id,
		open:   nil,
		flags:  0,
		layout: layout,
		push:   nil,
		pop:    nil,
	}
}
