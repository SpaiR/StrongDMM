package app

import (
	"log"
	"sdmm/app/ui/cpwsarea/wsprefs"
)

func (a *app) makePreferences() wsprefs.Prefs {
	prefs := wsprefs.MakePrefs()
	prefs.Add(wsprefs.GPInterface, a.makePreferenceInterfaceScale())
	prefs.Add(wsprefs.GPControls, a.makePreferenceControlsAltScrollBehaviour())
	return prefs
}

func (a *app) makePreferenceInterfaceScale() wsprefs.IntPref {
	p := wsprefs.NewIntPref()
	p.Name = "Interface Scale"
	p.Desc = "Controls the interface scale."
	p.Label = "%##preference_scale"
	p.Min = 50
	p.Max = 250

	p.FGet = func() int {
		return a.preferencesConfig().Prefs.Interface.Scale
	}
	p.FSet = func(value int) {
		log.Println("[app] preferences changing, [interface#scale] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Interface.Scale = value
		a.ConfigSaveV(cfg)
		a.tmpUpdateScale = true
	}

	return p
}

func (a *app) makePreferenceControlsAltScrollBehaviour() wsprefs.BoolPref {
	p := wsprefs.NewBoolPref()
	p.Name = "Alternative Scroll Behavior"
	p.Desc = "When enabled, scrolling will do panning. Zoom will be available if a Space key pressed."
	p.Label = "##alternative_scroll_behavior"

	p.FGet = func() bool {
		return a.preferencesConfig().Prefs.Controls.AltScrollBehaviour
	}
	p.FSet = func(value bool) {
		log.Println("[app] preferences changing, [controls#alternative_scroll_behaviour] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Controls.AltScrollBehaviour = value
		a.ConfigSaveV(cfg)
	}

	return p
}
