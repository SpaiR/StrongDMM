package app

import (
	"log"
	"math"
	"sdmm/app/prefs"
	"sdmm/app/ui/cpwsarea/wsprefs"
	"sdmm/app/window"
)

func (a *app) makePreferences() wsprefs.Prefs {
	p := wsprefs.MakePrefs()
	p.Add(wsprefs.GPEditor, a.makePreferenceEditorFormat())
	p.Add(wsprefs.GPEditor, a.makePreferenceEditorSanitizeVariables())
	p.Add(wsprefs.GPEditor, a.makePreferenceEditorNudgeMode())
	p.Add(wsprefs.GPControls, a.makePreferenceControlsAltScrollBehaviour())
	p.Add(wsprefs.GPControls, a.makePreferenceControlsQuickEditContextMenu())
	p.Add(wsprefs.GPControls, a.makePreferenceControlsQuickEditMapPane())
	p.Add(wsprefs.GPInterface, a.makePreferenceInterfaceScale())
	p.Add(wsprefs.GPInterface, a.makePreferenceInterfaceFps())
	p.Add(wsprefs.GPApplication, a.makePreferenceApplicationCheckForUpdates())
	return p
}

func (a *app) makePreferenceInterfaceFps() wsprefs.IntPref {
	p := wsprefs.MakeIntPref()
	p.Name = "Fps"
	p.Desc = "Controls the application framerate."
	p.Label = "##fps"
	p.Min = 30
	p.Max = math.MaxInt

	p.FGet = func() int {
		return a.preferencesConfig().Prefs.Interface.Fps
	}
	p.FSet = func(value int) {
		log.Println("[app] preferences changing, [interface#fps] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Interface.Fps = value
		a.ConfigSaveV(cfg)
		window.SetFps(value)
	}

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

func (a *app) makePreferenceControlsQuickEditContextMenu() wsprefs.BoolPref {
	p := wsprefs.MakeBoolPref()
	p.Name = "Quick Edit: Tile Context Menu"
	p.Desc = "Controls whether Quick Edit should be shown in the tile context menu."
	p.Label = "##quick_edit:tile_context_menu"

	p.FGet = func() bool {
		return a.preferencesConfig().Prefs.Controls.QuickEditContextMenu
	}
	p.FSet = func(value bool) {
		log.Println("[app] preferences changing, [controls#quick_edit:tile_context_menu] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Controls.QuickEditContextMenu = value
		a.ConfigSaveV(cfg)
	}

	return p
}

func (a *app) makePreferenceControlsQuickEditMapPane() wsprefs.BoolPref {
	p := wsprefs.MakeBoolPref()
	p.Name = "Quick Edit: Map Pane"
	p.Desc = "Controls whether Quick Edit should be shown on the map pane."
	p.Label = "##quick_edit:map_pane"

	p.FGet = func() bool {
		return a.preferencesConfig().Prefs.Controls.QuickEditMapPane
	}
	p.FSet = func(value bool) {
		log.Println("[app] preferences changing, [controls#quick_edit:map_pane] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Controls.QuickEditMapPane = value
		a.ConfigSaveV(cfg)
	}

	return p
}

func (a *app) makePreferenceEditorFormat() wsprefs.OptionPref {
	p := wsprefs.MakeOptionPref()
	p.Name = "Save Format"
	p.Desc = "Controls the format used by the editor to save the map."
	p.Label = "##save_format"

	p.FGet = func() string {
		return a.preferencesConfig().Prefs.Editor.SaveFormat
	}
	p.FSet = func(value string) {
		log.Println("[app] preferences changing, [editor#save_format] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Editor.SaveFormat = value
		a.ConfigSaveV(cfg)
	}

	p.Options = prefs.SaveFormats
	p.Help = prefs.SaveFormatHelp

	return p
}

func (a *app) makePreferenceEditorSanitizeVariables() wsprefs.BoolPref {
	p := wsprefs.MakeBoolPref()
	p.Name = "Sanitize Variables"
	p.Desc = "Enables sanitizing for variables which are declared on the map, but has the same value as initial."
	p.Label = "##sanitize_variables"

	p.FGet = func() bool {
		return a.preferencesConfig().Prefs.Editor.SanitizeVariables
	}
	p.FSet = func(value bool) {
		log.Println("[app] preferences changing, [editor#sanitize_variables] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Editor.SanitizeVariables = value
		a.ConfigSaveV(cfg)
	}

	return p
}

func (a *app) makePreferenceEditorNudgeMode() wsprefs.OptionPref {
	p := wsprefs.MakeOptionPref()
	p.Name = "Nudge Mode"
	p.Desc = "Controls which variables will be changed during the nudge."
	p.Label = "##nudge_mode"

	p.FGet = func() string {
		return a.preferencesConfig().Prefs.Editor.NudgeMode
	}
	p.FSet = func(value string) {
		log.Println("[app] preferences changing, [editor#nudge_mode] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Editor.NudgeMode = value
		a.ConfigSaveV(cfg)
	}

	p.Options = prefs.SaveNudgeModes

	return p
}

func (a *app) makePreferenceApplicationCheckForUpdates() wsprefs.BoolPref {
	p := wsprefs.MakeBoolPref()
	p.Name = "Check for Updates"
	p.Desc = "When enabled, the editor will always check for updates on startup."
	p.Label = "##check_for_updates"

	p.FGet = func() bool {
		return a.preferencesConfig().Prefs.Application.CheckForUpdates
	}
	p.FSet = func(value bool) {
		log.Println("[app] preferences changing, [application#check_for_updates] to:", value)
		cfg := a.preferencesConfig()
		cfg.Prefs.Application.CheckForUpdates = value
		a.ConfigSaveV(cfg)
	}

	return p
}
