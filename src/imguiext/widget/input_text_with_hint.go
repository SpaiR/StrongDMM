package widget

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
)

type inputTextWithHintWidget struct {
	inputTextWidget
	hint string
}

func (i *inputTextWithHintWidget) ButtonClear() *inputTextWithHintWidget {
	fClear := func() {
		*i.text = ""
	}
	i.Button(Button(icon.Clear+"##"+i.label, fClear).
		TextColor(imgui.CurrentStyle().Color(imgui.StyleColorTextDisabled)).
		Style(style.ButtonFrame{}),
	)
	return i
}

func InputTextWithHint(label, hint string, text *string) *inputTextWithHintWidget {
	i := &inputTextWithHintWidget{hint: hint}
	i.label = label
	i.text = text
	i.inputTextFunc = func() bool {
		return imgui.InputTextWithHintV(i.label, i.hint, i.text, i.flags, i.cb)
	}
	return i
}
