package app

import (
	"log"

	"sdmm/app/ui/cpwsarea/workspace/wsprefs"
)

func (a *app) makePreferences() wsprefs.Prefs {
	prefs := wsprefs.MakePrefs()
	prefs.Add(wsprefs.GPInterface, a.makeScalePreference())
	return prefs
}

func (a *app) makeScalePreference() wsprefs.IntPref {
	scale := wsprefs.NewIntPref()
	scale.Name = "Scale"
	scale.Desc = "Controls the interface scale."
	scale.Label = "%##preference_scale"
	scale.Min = 50
	scale.Max = 250

	scale.FGet = func() int {
		return a.prefsData.Scale
	}
	scale.FSet = func(value int) {
		a.prefsData.Scale = value
		a.prefsData.Save()
		a.tmpUpdateScale = true
		log.Println("[app] changing scale to:", value)
	}

	return scale
}