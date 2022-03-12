package app

import (
	"log"
	"sdmm/app/prefs"
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

func (preferencesConfig) TryMigrate(cfg map[string]interface{}) (result map[string]interface{}, migrated bool) {
	result = cfg

	if uint(result["Version"].(float64)) == 1 {
		result["Editor"] = result["Save"]
		delete(result, "Save")

		editorPrefs := result["Editor"].(map[string]interface{})
		editorPrefs["Save Format"] = editorPrefs["Format"]
		delete(editorPrefs, "Format")

		result["Version"] = 2
		migrated = true
	}

	return result, migrated
}

func (a *app) loadPreferencesConfig() {
	a.ConfigRegister(&preferencesConfig{
		Version: preferencesConfigVersion,

		Prefs: prefs.Prefs{
			Interface: prefs.Interface{
				Scale: 100,
			},
			Editor: prefs.Editor{
				SaveFormat: prefs.SaveFormatInitial,
				NudgeMode:  prefs.SaveNudgeModePixel,
			},
		},
	})
}

func (a *app) preferencesConfig() *preferencesConfig {
	if cfg, ok := a.ConfigFind(preferencesConfigName).(*preferencesConfig); ok {
		return cfg
	}
	log.Fatal("[app] can't find project config")
	return nil
}
