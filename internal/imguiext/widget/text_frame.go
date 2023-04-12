package widget

import "github.com/SpaiR/imgui-go"

type textFrameWidget struct {
	buttonWidget *ButtonWidget

	width float32
}

func (w *textFrameWidget) Width(width float32) *textFrameWidget {
	w.width = width
	return w
}

func (w *textFrameWidget) Build() {
	if w.width != 0 {
		w.buttonWidget.size = imgui.Vec2{X: w.width}
	}
	col := imgui.CurrentStyle().Color(imgui.StyleColorButton)
	col.W = .5
	w.buttonWidget.
		NormalColor(col).
		ActiveColor(col).
		HoverColor(col).
		Build()
}

func (w *textFrameWidget) CalcSize() imgui.Vec2 {
	return w.buttonWidget.CalcSize()
}

func TextFrame(text string) *textFrameWidget {
	return &textFrameWidget{
		buttonWidget: Button(text, nil),
	}
}
