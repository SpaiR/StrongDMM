package widget

import "github.com/SpaiR/imgui-go"

type inputTextMultilineWidget struct {
	label         string
	text          *string
	width, height float32
	flags         imgui.InputTextFlags
	cb            imgui.InputTextCallback
	onChange      func()
}

func (i *inputTextMultilineWidget) Size(width, height float32) *inputTextMultilineWidget {
	i.width = width
	i.height = height
	return i
}

func (i *inputTextMultilineWidget) Flags(flags imgui.InputTextFlags) *inputTextMultilineWidget {
	i.flags = flags
	return i
}

func (i *inputTextMultilineWidget) Callback(cb imgui.InputTextCallback) *inputTextMultilineWidget {
	i.cb = cb
	return i
}

func (i *inputTextMultilineWidget) OnChange(onChange func()) *inputTextMultilineWidget {
	i.onChange = onChange
	return i
}

func (i *inputTextMultilineWidget) Build() {
	if imgui.InputTextMultilineV(i.label, i.text, imgui.Vec2{X: i.width, Y: i.height}, i.flags, i.cb) && i.onChange != nil {
		i.onChange()
	}
}

func InputTextMultiline(label string, text *string) *inputTextMultilineWidget {
	return &inputTextMultilineWidget{
		label:    label,
		text:     text,
		width:    0,
		height:   0,
		flags:    imgui.InputTextFlagsNone,
		cb:       nil,
		onChange: nil,
	}
}
