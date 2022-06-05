package wsprefs

import (
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/window"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/markdown"
	"sdmm/imguiext/style"

	"github.com/SpaiR/imgui-go"
)

type WsPrefs struct {
	workspace.Content

	prefs Prefs
}

func New(prefs Prefs) *WsPrefs {
	return &WsPrefs{
		prefs: prefs,
	}
}

func (ws *WsPrefs) Name() string {
	return icon.Wrench + " Preferences"
}

func (ws *WsPrefs) Title() string {
	return "Preferences"
}

func (ws *WsPrefs) Process() {
	ws.showContent()
}

func (ws *WsPrefs) showContent() {
	for idx, group := range prefsGroupOrder {
		if idx > 0 {
			imgui.NewLine()
		}

		markdown.ShowHeader(string(group), window.FontH2)
		imgui.Separator()

		for idx, pref := range ws.prefs[group] {
			if idx > 0 {
				imgui.NewLine()
			}
			imgui.PushID(string(group))
			if pref, ok := pref.(IntPref); ok {
				showIntPref(pref)
			}
			if pref, ok := pref.(BoolPref); ok {
				showBoolPref(pref)
			}
			if pref, ok := pref.(OptionPref); ok {
				showOptionPref(pref)
			}
			imgui.PopID()
		}
	}
}

func showIntPref(pref IntPref) {
	markdown.ShowHeaderV(pref.Name, window.FontH3, style.ColorWhite)

	imgui.PushTextWrapPos()
	imgui.TextDisabled(pref.Desc)
	showHelp(pref.Help)
	imgui.PopTextWrapPos()

	v := int32(pref.FGet())
	if imguiext.InputIntClamp(pref.Label, &v, pref.Min, pref.Max, pref.Step, pref.StepFast) {
		if int(v) != pref.FGet() {
			pref.FSet(int(v))
		}
	}
}

func showBoolPref(pref BoolPref) {
	fToggle := func() {
		pref.FSet(!pref.FGet())
	}

	markdown.ShowHeaderV(pref.Name, window.FontH3, style.ColorWhite)

	imgui.PushStyleVarVec2(imgui.StyleVarFramePadding, imgui.Vec2{X: window.PointSize(), Y: window.PointSize()})
	v := pref.FGet()
	if imgui.Checkbox(pref.Label, &v) {
		fToggle()
	}
	imgui.PopStyleVar()

	imgui.SameLine()

	imgui.PushTextWrapPos()
	imgui.TextDisabled(pref.Desc)
	showHelp(pref.Help)
	imgui.PopTextWrapPos()

	if imgui.IsItemHovered() {
		imgui.SetMouseCursor(imgui.MouseCursorHand)
	}

	if imgui.IsItemClicked() {
		fToggle()
	}
}

func showOptionPref(pref OptionPref) {
	markdown.ShowHeaderV(pref.Name, window.FontH3, style.ColorWhite)

	imgui.PushTextWrapPos()
	imgui.TextDisabled(pref.Desc)
	showHelp(pref.Help)
	imgui.PopTextWrapPos()

	if imgui.BeginCombo(pref.Label, pref.FGet()) {
		for _, option := range pref.Options {
			if imgui.SelectableV(option, option == pref.FGet(), imgui.SelectableFlagsNone, imgui.Vec2{}) {
				pref.FSet(option)
			}
		}
		imgui.EndCombo()
	}
}

func showHelp(helpText string) {
	if helpText != "" {
		imgui.SameLine()
		imgui.TextDisabled(icon.Help)
		imguiext.SetItemHoveredTooltip(helpText)
	}
}
