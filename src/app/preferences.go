package app

import (
	"log"
	"sdmm/app/prefs"
	"sdmm/app/ui/cpwsarea/wsprefs"
)

func (a *app) makePreferences() wsprefs.Prefs {
	p := wsprefs.MakePrefs()
	p.Add(wsprefs.GPInterface, a.makePreferenceInterfaceScale())
	p.Add(wsprefs.GPControls, a.makePreferenceControlsAltScrollBehaviour())
	p.Add(wsprefs.GPSave, a.makePreferenceSaveFormat())
	p.Add(wsprefs.GPSave, a.makePreferenceSaveSanitizeVariables())
	p.Add(wsprefs.GPSave, a.makePreferenceSaveNudgeMode())
	return p
}

func (a *app) makePreferenceInterfaceScale() wsprefs.IntPref {
	p := wsprefs.MakeIntPref()
	p.Name = "Scale"
	p.Desc = "Controls the interface scale."
	p.Label = "%##scale"
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
	p := wsprefs.MakeBoolPref()
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

func (a *app) makePreferenceSaveFormat() wsprefs.OptionPref {
	p := wsprefs.MakeOptionPref()
	p.Name = "Format"
	p.Desc = "Controls the format used by the editor to save the map."
	p.Label = "##format"

	p.FGet = func() string {
		return a.preferencesConfig().Prefs.Save.Format
	}
	p.FSet = func(value string) {
		log.Println("[app] preferences changing, [save#format] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Save.Format = value
		a.ConfigSaveV(cfg)
	}

	p.Options = prefs.SaveFormats
	p.Help = prefs.SaveFormatHelp

	return p
}

func (a *app) makePreferenceSaveSanitizeVariables() wsprefs.BoolPref {
	p := wsprefs.MakeBoolPref()
	p.Name = "Sanitize Variables"
	p.Desc = "Enables sanitizing for variables which are declared on the map, but has the same value as initial."
	p.Label = "##sanitize_variables"

	p.FGet = func() bool {
		return a.preferencesConfig().Prefs.Save.SanitizeVariables
	}
	p.FSet = func(value bool) {
		log.Println("[app] preferences changing, [save#sanitize_variables] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Save.SanitizeVariables = value
		a.ConfigSaveV(cfg)
	}

	return p
}

func (a *app) makePreferenceSaveNudgeMode() wsprefs.OptionPref {
	p := wsprefs.MakeOptionPref()
	p.Name = "Nudge Mode"
	p.Desc = "Controls which variables will be changed during the nudge."
	p.Label = "##nudge_mode"

	p.FGet = func() string {
		return a.preferencesConfig().Prefs.Save.NudgeMode
	}
	p.FSet = func(value string) {
		log.Println("[app] preferences changing, [save#nudge_mode] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Save.NudgeMode = value
		a.ConfigSaveV(cfg)
	}

	p.Options = prefs.SaveNudgeModes

	return p
}
