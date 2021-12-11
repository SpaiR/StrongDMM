package widget

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext/icon"
)

type inputTextWithHintWidget struct {
	label    string
	hint     string
	text     *string
	clearBtn string
	width    float32
	flags    imgui.InputTextFlags
	cb       imgui.InputTextCallback
	onChange func()
}

func (i *inputTextWithHintWidget) ClearBtn() *inputTextWithHintWidget {
	i.clearBtn = icon.FaTimes + "##" + i.label
	return i
}

func (i *inputTextWithHintWidget) Width(width float32) *inputTextWithHintWidget {
	i.width = width
	return i
}

func (i *inputTextWithHintWidget) Flags(flags imgui.InputTextFlags) *inputTextWithHintWidget {
	i.flags = flags
	return i
}

func (i *inputTextWithHintWidget) Callback(cb imgui.InputTextCallback) *inputTextWithHintWidget {
	i.cb = cb
	return i
}

func (i *inputTextWithHintWidget) OnChange(onChange func()) *inputTextWithHintWidget {
	i.onChange = onChange
	return i
}

func (i *inputTextWithHintWidget) Build() {
	widgetWidth := i.width

	var clearBtn *buttonWidget
	if i.clearBtn != "" && widgetWidth != 0 {
		clearBtn = Button(i.clearBtn, func() { *i.text = "" }).
			TextColor(imgui.CurrentStyle().Color(imgui.StyleColorTextDisabled)).
			NormalColor(imgui.CurrentStyle().Color(imgui.StyleColorFrameBg)).
			HoverColor(imgui.CurrentStyle().Color(imgui.StyleColorFrameBgHovered)).
			ActiveColor(imgui.CurrentStyle().Color(imgui.StyleColorFrameBgActive))
		if widgetWidth == -1 {
			widgetWidth = -clearBtn.CalcSize().X
		} else {
			widgetWidth -= clearBtn.CalcSize().X
		}
	}

	if widgetWidth != 0 {
		imgui.SetNextItemWidth(widgetWidth)
	}

	if clearBtn != nil {
		imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, imgui.Vec2{Y: imgui.CurrentStyle().ItemSpacing().Y})
	}

	if imgui.InputTextWithHintV(i.label, i.hint, i.text, i.flags, i.cb) && i.onChange != nil {
		i.onChange()
	}

	if clearBtn != nil {
		imgui.SameLine()
		clearBtn.Build()
		imgui.PopStyleVar()
	}
}

func InputTextWithHint(label, hint string, text *string) *inputTextWithHintWidget {
	return &inputTextWithHintWidget{
		label:    label,
		hint:     hint,
		text:     text,
		clearBtn: "",
		width:    0,
		flags:    imgui.InputTextFlagsNone,
		cb:       nil,
		onChange: nil,
	}
}
