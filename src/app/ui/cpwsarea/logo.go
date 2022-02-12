package cpwsarea

import (
	"github.com/SpaiR/imgui-go"
	"image/color"
	"sdmm/app/window"
)

const logoSize = 250

var (
	logoColor        = imgui.Packed(color.RGBA{R: 200, G: 200, B: 200, A: 75})
	logoColorHovered = imgui.Packed(color.RGBA{R: 200, G: 200, B: 200, A: 125})
)

// Show application logo in the center of the window.
func (w *WsArea) showAppLogo(dockId int) {
	imgui.SetNextWindowDockIDV(dockId, imgui.ConditionAlways)
	imgui.ExtSetNextWindowDockNodeFlags(imgui.DockNodeFlagsNoTabBar)
	if imgui.BeginV("workspace_area_help", nil, imgui.WindowFlagsNoSavedSettings|imgui.WindowFlagsNoDecoration) {
		winSize := imgui.WindowSize()
		winPos := imgui.WindowPos()

		size := imgui.Vec2{X: logoSize * w.app.PointSize(), Y: logoSize * w.app.PointSize()}
		pos := winPos.Plus(winSize.Minus(size).Times(.5))

		imgui.SetCursorScreenPos(pos)
		imgui.Dummy(size)

		var aColor imgui.PackedColor
		if imgui.IsItemHovered() {
			imgui.SetMouseCursor(imgui.MouseCursorHand)
			aColor = logoColorHovered
		} else {
			aColor = logoColor
		}

		if imgui.IsItemClicked() {
			w.AddEmptyWorkspace()
		}

		imgui.WindowDrawList().AddImageV(
			imgui.TextureID(window.AppLogoTexture),
			pos,
			pos.Plus(size),
			imgui.Vec2{},
			imgui.Vec2{X: 1, Y: 1},
			aColor,
		)
	}
	imgui.End()
}
