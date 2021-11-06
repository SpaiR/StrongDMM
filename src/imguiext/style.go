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

type StyleButtonGold struct {
}

func (StyleButtonGold) NormalColor() imgui.Vec4 {
	return ColorGold
}

func (StyleButtonGold) ActiveColor() imgui.Vec4 {
	return ColorGoldDarker
}

func (StyleButtonGold) HoverColor() imgui.Vec4 {
	return ColorGoldLighter
}

type StyleButtonRed struct {
}

func (StyleButtonRed) NormalColor() imgui.Vec4 {
	return ColorRed
}

func (StyleButtonRed) ActiveColor() imgui.Vec4 {
	return ColorRedDarker
}

func (StyleButtonRed) HoverColor() imgui.Vec4 {
	return ColorRedLighter
}
