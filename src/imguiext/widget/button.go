package widget

import "github.com/SpaiR/imgui-go"

type ButtonStyle interface {
	NormalColor() imgui.Vec4
	ActiveColor() imgui.Vec4
	HoverColor() imgui.Vec4
}

type buttonWidget struct {
	label       string
	size        imgui.Vec2
	textColor   imgui.Vec4
	normalColor imgui.Vec4
	activeColor imgui.Vec4
	hoverColor  imgui.Vec4
	onClick     func()
}

func (b *buttonWidget) Size(size imgui.Vec2) *buttonWidget {
	b.size = size
	return b
}

func (b *buttonWidget) Style(style ButtonStyle) *buttonWidget {
	return b.NormalColor(style.NormalColor()).
		ActiveColor(style.ActiveColor()).
		HoverColor(style.HoverColor())
}

func (b *buttonWidget) TextColor(color imgui.Vec4) *buttonWidget {
	b.textColor = color
	return b
}

func (b *buttonWidget) NormalColor(color imgui.Vec4) *buttonWidget {
	b.normalColor = color
	return b
}

func (b *buttonWidget) ActiveColor(color imgui.Vec4) *buttonWidget {
	b.activeColor = color
	return b
}

func (b *buttonWidget) HoverColor(color imgui.Vec4) *buttonWidget {
	b.hoverColor = color
	return b
}

func (b *buttonWidget) CalcSize() (size imgui.Vec2) {
	labelSize := imgui.CalcTextSize(b.label, true, -1)
	padding := imgui.CurrentStyle().FramePadding()
	return size.Plus(labelSize).Plus(b.size).Plus(padding.Times(2))
}

func (b *buttonWidget) Build() {
	imgui.PushStyleColor(imgui.StyleColorText, b.textColor)
	imgui.PushStyleColor(imgui.StyleColorButton, b.normalColor)
	imgui.PushStyleColor(imgui.StyleColorButtonActive, b.activeColor)
	imgui.PushStyleColor(imgui.StyleColorButtonHovered, b.hoverColor)
	if imgui.ButtonV(b.label, b.size) && b.onClick != nil {
		b.onClick()
	}
	imgui.PopStyleColorV(4)
}

func Button(label string, onClick func()) *buttonWidget {
	return &buttonWidget{
		label:       label,
		size:        imgui.Vec2{},
		textColor:   imgui.CurrentStyle().Color(imgui.StyleColorText),
		normalColor: imgui.CurrentStyle().Color(imgui.StyleColorButton),
		activeColor: imgui.CurrentStyle().Color(imgui.StyleColorButtonActive),
		hoverColor:  imgui.CurrentStyle().Color(imgui.StyleColorButtonHovered),
		onClick:     onClick,
	}
}
