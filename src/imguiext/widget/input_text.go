package widget

import "github.com/SpaiR/imgui-go"

type inputTextWidget struct {
	label                  string
	text                   *string
	width                  float32
	button                 *ButtonWidget
	flags                  imgui.InputTextFlags
	cb                     imgui.InputTextCallback
	onChange               func()
	onDeactivatedAfterEdit func()
}

func (i *inputTextWidget) Width(width float32) *inputTextWidget {
	i.width = width
	return i
}

func (i *inputTextWidget) Button(button *ButtonWidget) *inputTextWidget {
	i.button = button
	return i
}

func (i *inputTextWidget) Flags(flags imgui.InputTextFlags) *inputTextWidget {
	i.flags = flags
	return i
}

func (i *inputTextWidget) Callback(cb imgui.InputTextCallback) *inputTextWidget {
	i.cb = cb
	return i
}

func (i *inputTextWidget) OnChange(onChange func()) *inputTextWidget {
	i.onChange = onChange
	return i
}

func (i *inputTextWidget) OnDeactivatedAfterEdit(onDeactivatedAfterEdit func()) *inputTextWidget {
	i.onDeactivatedAfterEdit = onDeactivatedAfterEdit
	return i
}

func (i *inputTextWidget) Build() {
	widgetWidth := i.width

	if i.button != nil && widgetWidth != 0 {
		if widgetWidth == -1 {
			widgetWidth = -i.button.CalcSize().X
		} else {
			widgetWidth -= i.button.CalcSize().X
		}
	}

	if widgetWidth != 0 {
		imgui.SetNextItemWidth(widgetWidth)
	}

	if i.button != nil {
		imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, imgui.Vec2{Y: imgui.CurrentStyle().ItemSpacing().Y})
	}

	if imgui.InputTextV(i.label, i.text, i.flags, i.cb) && i.onChange != nil {
		i.onChange()
	}
	if i.onDeactivatedAfterEdit != nil && imgui.IsItemDeactivatedAfterEdit() {
		i.onDeactivatedAfterEdit()
	}

	if i.button != nil {
		imgui.SameLine()
		i.button.Build()
		imgui.PopStyleVar()
	}
}

func InputText(label string, text *string) *inputTextWidget {
	return &inputTextWidget{
		label: label,
		text:  text,
	}
}
