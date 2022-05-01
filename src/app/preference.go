package app

import (
	"math" //nolint

	"sdmm/app/prefs" //nolint
	"sdmm/app/ui/cpwsarea/wsprefs"
	"sdmm/app/window" //nolint
)

func (a *app) makePreferences() wsprefs.Prefs {
	p := wsprefs.MakePrefs()

	var preferencesPrefabs = map[wsprefs.PrefGroup][]prefPrefab{
		wsprefs.GPEditor: {
			optionPrefPrefab{
				name:    "Save Format",
				desc:    "Controls the format used by the editor to save the map.",
				label:   "##save_format",
				value:   &a.preferencesConfig().Prefs.Editor.SaveFormat,
				options: prefs.SaveFormats,
				help:    prefs.SaveFormatHelp,
			},
			boolPrefPrefab{
				name:  "Sanitize Variables",
				desc:  "Enables sanitizing for variables which are declared on the map, but has the same value as initial.",
				label: "##sanitize_variables",
				value: &a.preferencesConfig().Prefs.Editor.SanitizeVariables,
			},
			optionPrefPrefab{
				name:    "Nudge Mode",
				desc:    "Controls which variables will be changed during the nudge.",
				label:   "##nudge_mode",
				value:   &a.preferencesConfig().Prefs.Editor.NudgeMode,
				options: prefs.SaveNudgeModes,
			},
		},

		wsprefs.GPControls: {
			boolPrefPrefab{
				name:  "Alternative Scroll Behavior",
				desc:  "When enabled, scrolling will do panning. Zoom will be available if a Space key pressed.",
				label: "##alternative_scroll_behavior",
				value: &a.preferencesConfig().Prefs.Controls.AltScrollBehaviour,
			},
			boolPrefPrefab{
				name:  "Quick Edit: Tile Context Menu",
				desc:  "Controls whether Quick Edit should be shown in the tile context menu.",
				label: "##quick_edit:tile_context_menu",
				value: &a.preferencesConfig().Prefs.Controls.QuickEditContextMenu,
			},
			boolPrefPrefab{
				name:  "Quick Edit: Map Pane",
				desc:  "Controls whether Quick Edit should be shown on the map pane.",
				label: "##quick_edit:map_pane",
				value: &a.preferencesConfig().Prefs.Controls.QuickEditMapPane,
			},
		},

		wsprefs.GPInterface: {
			intPrefPrefab{
				name:  "Scale",
				desc:  "Controls the interface scale.",
				label: "%##scale",
				min:   50,
				max:   250,
				value: &a.preferencesConfig().Prefs.Interface.Scale,
				post: func(int) {
					a.tmpUpdateScale = true
				},
			},
			intPrefPrefab{
				name:  "Fps",
				desc:  "Controls the application framerate.",
				label: "##fps",
				min:   30,
				max:   math.MaxInt,
				value: &a.preferencesConfig().Prefs.Interface.Fps,
				post:  window.SetFps,
			},
		},

		wsprefs.GPApplication: {
			boolPrefPrefab{
				name:  "Check for Updates",
				desc:  "When enabled, the editor will always check for updates on startup.",
				label: "##check_for_updates",
				value: &a.preferencesConfig().Prefs.Application.CheckForUpdates,
			},
		},
	}

	for group, prefabs := range preferencesPrefabs {
		for _, prefab := range prefabs {
			p.Add(group, prefab.make())
		}
	}

	return p
}
