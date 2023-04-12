package widget

import "github.com/SpaiR/imgui-go"

type Layout []widget

func (layout Layout) Build() {
	if align, ok := layout[0].(LayoutAlign); ok {
		layout.BuildV(align)
	} else {
		layout.BuildV(AlignLeft)
	}
}

func (layout Layout) CalcSize() (size imgui.Vec2) {
	_, hasAlign := layout[0].(LayoutAlign)

	for idx, w := range layout {
		if r, ok := w.(region); ok {
			size = size.Plus(r.CalcSize())
			if hasAlign && idx > 1 || !hasAlign && idx > 0 {
				size = size.Plus(imgui.CurrentStyle().ItemSpacing())
			}
		}
	}
	return size
}

type LayoutAlign int

func (LayoutAlign) Build() {
	// mock widget behaviour
}

type region interface {
	CalcSize() imgui.Vec2
}

const (
	AlignLeft LayoutAlign = iota
	AlignCenter
	AlignRight
)

func (layout Layout) BuildV(align LayoutAlign) {
	var indent float32
	switch align {
	case AlignLeft:
		indent = 0
	case AlignRight:
		indent = imgui.WindowWidth() - imgui.CurrentStyle().WindowPadding().Times(2).X - layout.CalcSize().X
	}

	if indent != 0 {
		imgui.IndentV(indent)
	}
	for _, w := range layout {
		w.Build()
	}
	if indent != 0 {
		imgui.UnindentV(indent)
	}
}
