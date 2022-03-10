package widget

import "github.com/SpaiR/imgui-go"

type Layout []widget

func (layout Layout) Build() {
	layout.BuildV(AlignLeft)
}

func (layout Layout) CalcSize() (size imgui.Vec2) {
	for idx, w := range layout {
		if r, ok := w.(region); ok {
			size = size.Plus(r.CalcSize())
			if idx > 0 {
				size = size.Plus(imgui.CurrentStyle().ItemSpacing())
			}
		}
	}
	return size
}

type LayoutAlign int

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
	//imgui.BeginGroup()
	for _, w := range layout {
		w.Build()
	}
	//imgui.EndGroup()
	if indent != 0 {
		imgui.UnindentV(indent)
	}
}
