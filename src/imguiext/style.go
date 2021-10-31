package imguiext

import "github.com/SpaiR/imgui-go"

type StyleButtonGreen struct {
}

func (StyleButtonGreen) NormalColor() imgui.Vec4 {
	return ColorGreen1
}

func (StyleButtonGreen) ActiveColor() imgui.Vec4 {
	return ColorGreen1Darker
}

func (StyleButtonGreen) HoverColor() imgui.Vec4 {
	return ColorGreen1Lighter
}

type StyleButtonDefault struct {
}

func (StyleButtonDefault) NormalColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorButton)
}

func (StyleButtonDefault) ActiveColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorButtonActive)
}

func (StyleButtonDefault) HoverColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorButtonHovered)
}
