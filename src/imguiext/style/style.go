package style

import (
	"github.com/SpaiR/imgui-go"
)

type ButtonGreen struct {
}

func (ButtonGreen) NormalColor() imgui.Vec4 {
	return ColorGreen1
}

func (ButtonGreen) ActiveColor() imgui.Vec4 {
	return ColorGreen1Darker
}

func (ButtonGreen) HoverColor() imgui.Vec4 {
	return ColorGreen1Lighter
}

type ButtonDefault struct {
}

func (ButtonDefault) NormalColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorButton)
}

func (ButtonDefault) ActiveColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorButtonActive)
}

func (ButtonDefault) HoverColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorButtonHovered)
}

type ButtonGold struct {
}

func (ButtonGold) NormalColor() imgui.Vec4 {
	return ColorGold
}

func (ButtonGold) ActiveColor() imgui.Vec4 {
	return ColorGoldDarker
}

func (ButtonGold) HoverColor() imgui.Vec4 {
	return ColorGoldLighter
}

type ButtonRed struct {
}

func (ButtonRed) NormalColor() imgui.Vec4 {
	return ColorRed
}

func (ButtonRed) ActiveColor() imgui.Vec4 {
	return ColorRedDarker
}

func (ButtonRed) HoverColor() imgui.Vec4 {
	return ColorRedLighter
}

type ButtonTransparent struct {
}

func (ButtonTransparent) NormalColor() imgui.Vec4 {
	return ColorZero
}

func (ButtonTransparent) ActiveColor() imgui.Vec4 {
	return ColorZero
}

func (ButtonTransparent) HoverColor() imgui.Vec4 {
	return ColorZero
}

type ButtonFrame struct {
}

func (ButtonFrame) NormalColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorFrameBg)
}

func (ButtonFrame) ActiveColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorFrameBgActive)
}

func (ButtonFrame) HoverColor() imgui.Vec4 {
	return imgui.CurrentStyle().Color(imgui.StyleColorFrameBgHovered)
}
