package widget

import "github.com/SpaiR/imgui-go"

type indentWidget struct {
	indent  float32
	content Layout
}

func (w *indentWidget) Build() {
	imgui.IndentV(w.indent)
	w.content.Build()
	imgui.UnindentV(w.indent)
}

func Indent(indent float32, content Layout) *indentWidget {
	return &indentWidget{
		indent:  indent,
		content: content,
	}
}
