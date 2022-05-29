package widget

import "github.com/SpaiR/imgui-go"

type ButtonStyle interface {
	NormalColor() imgui.Vec4
	ActiveColor() imgui.Vec4
	HoverColor() imgui.Vec4
}

type ButtonWidget struct {
	icon        string
	label       string
	tooltip     string
	round       bool
	small       bool
	transparent bool
	mouseCursor imgui.MouseCursorID
	size        imgui.Vec2
	textColor   imgui.Vec4
	normalColor imgui.Vec4
	activeColor imgui.Vec4
	hoverColor  imgui.Vec4
	onClick     func()
}

func (w *ButtonWidget) Icon(icon string) *ButtonWidget {
	w.icon = icon
	return w
}

func (w *ButtonWidget) Tooltip(tooltip string) *ButtonWidget {
	w.tooltip = tooltip
	return w
}

func (w *ButtonWidget) Size(size imgui.Vec2) *ButtonWidget {
	w.size = size
	return w
}

func (w *ButtonWidget) Round(round bool) *ButtonWidget {
	w.round = round
	return w
}

func (w *ButtonWidget) Small(small bool) *ButtonWidget {
	w.small = small
	return w
}

func (w *ButtonWidget) Transparent(transparent bool) *ButtonWidget {
	w.transparent = transparent
	return w
}

func (w *ButtonWidget) Mouse(mouse imgui.MouseCursorID) *ButtonWidget {
	w.mouseCursor = mouse
	return w
}

func (w *ButtonWidget) Style(style ButtonStyle) *ButtonWidget {
	return w.NormalColor(style.NormalColor()).
		ActiveColor(style.ActiveColor()).
		HoverColor(style.HoverColor())
}

func (w *ButtonWidget) TextColor(color imgui.Vec4) *ButtonWidget {
	w.textColor = color
	return w
}

func (w *ButtonWidget) NormalColor(color imgui.Vec4) *ButtonWidget {
	w.normalColor = color
	return w
}

func (w *ButtonWidget) ActiveColor(color imgui.Vec4) *ButtonWidget {
	w.activeColor = color
	return w
}

func (w *ButtonWidget) HoverColor(color imgui.Vec4) *ButtonWidget {
	w.hoverColor = color
	return w
}

func (w *ButtonWidget) CalcSize() (size imgui.Vec2) {
	label := w.label
	if len(w.icon) > 0 {
		label = w.icon + " " + label
	}
	labelSize := imgui.CalcTextSize(label, true, -1)
	padding := imgui.CurrentStyle().FramePadding()
	return size.Plus(labelSize).Plus(w.size).Plus(padding.Times(2))
}

func (w *ButtonWidget) Build() {
	label := w.label
	if len(w.icon) > 0 {
		label = w.icon + " " + label
	}

	if w.round {
		imgui.PushStyleVarFloat(imgui.StyleVarFrameRounding, 12)
	}

	var (
		styleCounter int
		emptyColor   imgui.Vec4

		normalColor = w.normalColor
		activeColor = w.activeColor
		hoverColor  = w.hoverColor
	)

	if w.transparent {
		normalColor = emptyColor
		activeColor = emptyColor
		hoverColor = emptyColor
	}

	if w.textColor != bDefaultColor {
		imgui.PushStyleColor(imgui.StyleColorText, w.textColor)
		styleCounter++
	}
	if normalColor != bDefaultColor {
		imgui.PushStyleColor(imgui.StyleColorButton, normalColor)
		styleCounter++
	}
	if activeColor != bDefaultColor {
		imgui.PushStyleColor(imgui.StyleColorButtonActive, activeColor)
		styleCounter++
	}
	if hoverColor != bDefaultColor {
		imgui.PushStyleColor(imgui.StyleColorButtonHovered, hoverColor)
		styleCounter++
	}
	if w.transparent {
		imgui.PushStyleColor(imgui.StyleColorBorder, emptyColor)
		styleCounter++
	}

	var flag bool
	if w.small {
		flag = imgui.SmallButton(label)
	} else {
		flag = imgui.ButtonV(label, w.size)
	}

	imgui.PopStyleColorV(styleCounter)

	if w.round {
		imgui.PopStyleVar()
	}

	if flag && w.onClick != nil {
		w.onClick()
	}

	if w.tooltip != "" && imgui.IsItemHovered() {
		imgui.SetTooltip(w.tooltip)
	}

	if imgui.IsItemHovered() {
		imgui.SetMouseCursor(w.mouseCursor)
	}
}

var bDefaultColor = imgui.Vec4{X: -99, Y: -99, Z: -99, W: -99}

func Button(label string, onClick func()) *ButtonWidget {
	return &ButtonWidget{
		icon:        "",
		label:       label,
		tooltip:     "",
		size:        imgui.Vec2{},
		mouseCursor: imgui.MouseCursorArrow,
		textColor:   bDefaultColor,
		normalColor: bDefaultColor,
		activeColor: bDefaultColor,
		hoverColor:  bDefaultColor,
		onClick:     onClick,
	}
}
