package widget

import "github.com/SpaiR/imgui-go"

type LineWidget struct {
	content Layout
}

func (w *LineWidget) CalcSize() imgui.Vec2 {
	return w.content.CalcSize()
}

func (w *LineWidget) Build() {
	cnt := Layout{}
	for idx, l := range w.content {
		if idx != len(w.content)-1 {
			cnt = append(cnt, l, SameLine())
		} else {
			cnt = append(cnt, l)
		}
	}
	cnt.Build()
}

func Line(content ...widget) *LineWidget {
	return &LineWidget{
		content: content,
	}
}
