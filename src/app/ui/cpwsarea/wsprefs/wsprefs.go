package wsprefs

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
)

type WsPrefs struct {
	workspace.Content

	prefs Prefs
}

func New(prefs Prefs) *WsPrefs {
	return &WsPrefs{prefs: prefs}
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
	for _, group := range prefsGroupOrder {
		imgui.Text(string(group))
		imgui.Separator()
		for _, pref := range ws.prefs[group] {
			if pref, ok := pref.(IntPref); ok {
				showIntPref(pref)
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
