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
	mouseCursor imgui.MouseCursorID
	size        imgui.Vec2
	textColor   imgui.Vec4
	normalColor imgui.Vec4
	activeColor imgui.Vec4
	hoverColor  imgui.Vec4
	onClick     func()
}

func (b *ButtonWidget) Icon(icon string) *ButtonWidget {
	b.icon = icon
	return b
}

func (b *ButtonWidget) Tooltip(tooltip string) *ButtonWidget {
	b.tooltip = tooltip
	return b
}

func (b *ButtonWidget) Size(size imgui.Vec2) *ButtonWidget {
	b.size = size
	return b
}

func (b *ButtonWidget) Round(round bool) *ButtonWidget {
	b.round = round
	return b
}

func (b *ButtonWidget) Mouse(mouse imgui.MouseCursorID) *ButtonWidget {
	b.mouseCursor = mouse
	return b
}

func (b *ButtonWidget) Style(style ButtonStyle) *ButtonWidget {
	return b.NormalColor(style.NormalColor()).
		ActiveColor(style.ActiveColor()).
		HoverColor(style.HoverColor())
}

func (b *ButtonWidget) TextColor(color imgui.Vec4) *ButtonWidget {
	b.textColor = color
	return b
}

func (b *ButtonWidget) NormalColor(color imgui.Vec4) *ButtonWidget {
	b.normalColor = color
	return b
}

func (b *ButtonWidget) ActiveColor(color imgui.Vec4) *ButtonWidget {
	b.activeColor = color
	return b
}

func (b *ButtonWidget) HoverColor(color imgui.Vec4) *ButtonWidget {
	b.hoverColor = color
	return b
}

func (b *ButtonWidget) CalcSize() (size imgui.Vec2) {
	labelSize := imgui.CalcTextSize(b.label, true, -1)
	padding := imgui.CurrentStyle().FramePadding()
	return size.Plus(labelSize).Plus(b.size).Plus(padding.Times(2))
}

func (b *ButtonWidget) Build() {
	label := b.label
	if len(b.icon) > 0 {
		label = b.icon + " " + label
	}

	if b.round {
		imgui.PushStyleVarFloat(imgui.StyleVarFrameRounding, 12)
	}
	imgui.PushStyleColor(imgui.StyleColorText, b.textColor)
	imgui.PushStyleColor(imgui.StyleColorButton, b.normalColor)
	imgui.PushStyleColor(imgui.StyleColorButtonActive, b.activeColor)
	imgui.PushStyleColor(imgui.StyleColorButtonHovered, b.hoverColor)
	if imgui.ButtonV(label, b.size) && b.onClick != nil {
		b.onClick()
	}
	if b.tooltip != "" && imgui.IsItemHovered() {
		imgui.SetTooltip(b.tooltip)
		imgui.SetMouseCursor(b.mouseCursor)
	}
	imgui.PopStyleColorV(4)
	if b.round {
		imgui.PopStyleVar()
	}
}

func Button(label string, onClick func()) *ButtonWidget {
	return &ButtonWidget{
		icon:        "",
		label:       label,
		tooltip:     "",
		size:        imgui.Vec2{},
		mouseCursor: imgui.MouseCursorArrow,
		textColor:   imgui.CurrentStyle().Color(imgui.StyleColorText),
		normalColor: imgui.CurrentStyle().Color(imgui.StyleColorButton),
		activeColor: imgui.CurrentStyle().Color(imgui.StyleColorButtonActive),
		hoverColor:  imgui.CurrentStyle().Color(imgui.StyleColorButtonHovered),
		onClick:     onClick,
	}
}
