package widget

import "github.com/SpaiR/imgui-go"

type SelectableWidget struct {
	label       string
	selected    bool
	flags       imgui.SelectableFlags
	size        imgui.Vec2
	mouseCursor imgui.MouseCursorID
	onClick     func()
}

func (w *SelectableWidget) Selected(selected bool) *SelectableWidget {
	w.selected = selected
	return w
}

func (w *SelectableWidget) Flags(flags imgui.SelectableFlags) *SelectableWidget {
	w.flags = flags
	return w
}

func (w *SelectableWidget) Size(size imgui.Vec2) *SelectableWidget {
	w.size = size
	return w
}

func (w *SelectableWidget) Mouse(mouse imgui.MouseCursorID) *SelectableWidget {
	w.mouseCursor = mouse
	return w
}

func (w *SelectableWidget) OnClick(onClick func()) *SelectableWidget {
	w.onClick = onClick
	return w
}

func (w *SelectableWidget) Build() {
	if imgui.SelectableV(w.label, w.selected, w.flags, w.size) {
		if w.onClick != nil {
			w.onClick()
		}
	}
	if imgui.IsItemHovered() {
		imgui.SetMouseCursor(w.mouseCursor)
	}
}

func Selectable(label string) *SelectableWidget {
	return &SelectableWidget{
		label:       label,
		mouseCursor: imgui.MouseCursorArrow,
	}
}
