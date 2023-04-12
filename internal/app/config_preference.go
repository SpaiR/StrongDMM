package app

import (
	"log"

	"sdmm/internal/app/prefs"
	"sdmm/internal/app/window"
)

const (
	preferencesConfigName    = "preferences"
	preferencesConfigVersion = 3
)

type preferencesConfig struct {
	Version uint
	prefs.Prefs
}

func (preferencesConfig) Name() string {
	return preferencesConfigName
}

func (preferencesConfig) TryMigrate(cfg map[string]any) (result map[string]any, migrated bool) {
	result = cfg
	version := uint(result["Version"].(float64))

	if version == 1 {
		log.Println("[app] migrating [preferences] config:", 2)

		result["Editor"] = result["Save"]
		delete(result, "Save")

		editorPrefs := result["Editor"].(map[string]any)
		editorPrefs["SaveFormat"] = editorPrefs["Format"]
		delete(editorPrefs, "Format")

		result["Version"] = 2
		migrated = true
	}
	if version == 2 {
		log.Println("[app] migrating [preferences] config:", 3)

		editorPrefs := result["Editor"].(map[string]any)
		saveFormat := editorPrefs["SaveFormat"].(string)

		if saveFormat == "DM" {
			editorPrefs["SaveFormat"] = "DMM"
		}

		result["Version"] = 3
		migrated = true
	}

	return
}

func (a *app) loadPreferencesConfig() {
	cfg := &preferencesConfig{
		Version: preferencesConfigVersion,

		Prefs: prefs.Prefs{
			Interface: prefs.Interface{
				Scale: 100,
				Fps:   60,
			},
			Controls: prefs.Controls{
				QuickEditMapPane: true,
			},
			Editor: prefs.Editor{
				SaveFormat: prefs.SaveFormatInitial,
				NudgeMode:  prefs.SaveNudgeModePixel,
			},
			Application: prefs.Application{
				CheckForUpdates: true,
				AutoUpdate:      true,
			},
		},
	}

	a.ConfigRegister(cfg)

	window.SetFps(cfg.Prefs.Interface.Fps)
}

func (a *app) preferencesConfig() *preferencesConfig {
	if cfg, ok := a.ConfigFind(preferencesConfigName).(*preferencesConfig); ok {
		return cfg
	}
	log.Fatal("[app] can't find project config")
	return nil
}
