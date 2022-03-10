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
	w.buttonWidget.Build()
}

func (w *textFrameWidget) CalcSize() imgui.Vec2 {
	return w.buttonWidget.CalcSize()
}

func TextFrame(text string) *textFrameWidget {
	return &textFrameWidget{
		buttonWidget: Button(text, nil).
			NormalColor(imgui.CurrentStyle().Color(imgui.StyleColorFrameBg)).
			ActiveColor(imgui.CurrentStyle().Color(imgui.StyleColorFrameBg)).
			HoverColor(imgui.CurrentStyle().Color(imgui.StyleColorFrameBg)),
	}
}
