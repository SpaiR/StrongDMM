package app

import (
	"log"
	wsprefs2 "sdmm/app/ui/cpwsarea/wsprefs"
)

func (a *app) makePreferences() wsprefs2.Prefs {
	prefs := wsprefs2.MakePrefs()
	prefs.Add(wsprefs2.GPInterface, a.makeScalePreference())
	return prefs
}

func (a *app) makeScalePreference() wsprefs2.IntPref {
	scale := wsprefs2.NewIntPref()
	scale.Name = "Scale"
	scale.Desc = "Controls the interface scale."
	scale.Label = "%##preference_scale"
	scale.Min = 50
	scale.Max = 250

	scale.FGet = func() int {
		return a.preferencesConfig().Scale
	}
	scale.FSet = func(value int) {
		cfg := a.preferencesConfig()
		cfg.Scale = value
		a.ConfigSaveV(cfg)
		a.tmpUpdateScale = true
		log.Println("[app] changing scale to:", value)
	}

	return scale
}
