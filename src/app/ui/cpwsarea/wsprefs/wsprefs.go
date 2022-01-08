package wsprefs

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
)

type App interface {
	PointSize() float32
}

type WsPrefs struct {
	workspace.Content

	app App

	prefs Prefs
}

func New(app App, prefs Prefs) *WsPrefs {
	return &WsPrefs{
		prefs: prefs,
		app:   app,
	}
}

func (ws *WsPrefs) Name() string {
	return icon.FaWrench + " Preferences"
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
		imgui.Text(string(group))
		imgui.Separator()
		for _, pref := range ws.prefs[group] {
			if pref, ok := pref.(IntPref); ok {
				showIntPref(pref)
			}
			if pref, ok := pref.(BoolPref); ok {
				showBoolPref(pref, ws.app.PointSize())
			}
		}
	}
}

func showIntPref(pref IntPref) {
	imgui.TextWrapped(pref.Name)

	imgui.PushTextWrapPos()
	imgui.TextDisabled(pref.Desc)
	imgui.PopTextWrapPos()

	v := int32(pref.FGet())
	if imguiext.InputIntClamp(pref.Label, &v, pref.Min, pref.Max, pref.Step, pref.StepFast) {
		if int(v) != pref.FGet() {
			pref.FSet(int(v))
		}
	}
}

func showBoolPref(pref BoolPref, pointSize float32) {
	fToggle := func() {
		pref.FSet(!pref.FGet())
	}

	imgui.TextWrapped(pref.Name)

	imgui.PushStyleVarVec2(imgui.StyleVarFramePadding, imgui.Vec2{X: pointSize, Y: pointSize})
	v := pref.FGet()
	if imgui.Checkbox(pref.Label, &v) {
		fToggle()
	}
	imgui.PopStyleVar()

	imgui.SameLine()

	imgui.PushTextWrapPos()
	imgui.TextDisabled(pref.Desc)
	imgui.PopTextWrapPos()

	if imgui.IsItemHovered() {
		imgui.SetMouseCursor(imgui.MouseCursorHand)
	}

	if imgui.IsItemClicked() {
		fToggle()
	}
}
