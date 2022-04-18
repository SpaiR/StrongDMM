package app

import (
	"log"
	"sdmm/app/prefs"
	"sdmm/app/window"
)

const (
	preferencesConfigName    = "preferences"
	preferencesConfigVersion = 2
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

	if uint(result["Version"].(float64)) == 1 {
		log.Println("[app] migrating [preferences] config:", 2)

		result["Editor"] = result["Save"]
		delete(result, "Save")

		editorPrefs := result["Editor"].(map[string]any)
		editorPrefs["SaveFormat"] = editorPrefs["Format"]
		delete(editorPrefs, "Format")

		result["Version"] = 2
		migrated = true
	}

	return result, migrated
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
