package widget

import "github.com/SpaiR/imgui-go"

type tooltipWidget struct {
	content Layout
	onHover bool
}

func (w *tooltipWidget) OnHover(onHover bool) *tooltipWidget {
	w.onHover = onHover
	return w
}

func (w *tooltipWidget) Build() {
	if w.onHover && imgui.IsItemHovered() || !w.onHover {
		imgui.BeginTooltip()
		w.content.Build()
		imgui.EndTooltip()
	}
}

func Tooltip(content ...widget) *tooltipWidget {
	return &tooltipWidget{
		content: content,
		onHover: true,
	}
}
