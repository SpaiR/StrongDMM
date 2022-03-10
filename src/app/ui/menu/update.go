package menu

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"
)

var loadingDotTypes = []string{".", "..", "...", "...."}

func (m *Menu) showUpdateMenu() {
	w.Button(icon.SystemUpdate, nil).
		Style(style.ButtonFrame{}).
		TextColor(style.ColorGreen1Lighter).
		Build()

	imguiext.SetItemHoveredTooltip("New update available!")

	imgui.OpenPopupOnItemClickV("update_menu", imgui.PopupFlagsMouseButtonLeft)

	if imgui.BeginPopup("update_menu") {
		imgui.TextColored(style.ColorGold, m.updateVersion)
		if len(m.updateDescription) > 0 {
			imgui.Text(m.updateDescription)
		}
		imgui.Separator()

		switch m.updateStatus {
		case upStatusAvailable:
			m.showUpdateLayout()
		case upStatusUpdating:
			dotType := loadingDotTypes[(int(imgui.Time()/0.25) & 3)]
			imgui.Text("Updating " + dotType)
		case upStatusUpdated:
			w.Button("Restart to Apply", m.app.DoRestart).Build()
		case upStatusError:
			imgui.TextColored(style.ColorRed, "Something went wrong.\nPlease try again later.")
			imgui.Separator()
			m.showUpdateLayout()
		}

		imgui.EndPopup()
	}
}

func (m *Menu) showUpdateLayout() {
	w.Layout{
		w.Button("Update", m.app.DoSelfUpdate).
			Style(style.ButtonGreen{}),
		w.SameLine(),
		w.Button("Hide", m.hideUpdateButton),
	}.Build()
}

func (m *Menu) hideUpdateButton() {
	m.updateStatus = upStatusNone
}
