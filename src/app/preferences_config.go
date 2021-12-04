package app

import "log"

const (
	preferencesConfigName    = "preferences"
	preferencesConfigVersion = 1
)

type preferencesConfig struct {
	Version uint
	Scale   int
}

func (preferencesConfig) Name() string {
	return preferencesConfigName
}

func (preferencesConfig) TryMigrate(_ map[string]interface{}) (result map[string]interface{}, migrated bool) {
	// nothing to do. yet...
	return nil, migrated
}

func (a *app) loadPreferencesConfig() {
	a.ConfigRegister(&preferencesConfig{
		Version: preferencesConfigVersion,
		Scale:   100,
	})
}

func (a *app) preferencesConfig() *preferencesConfig {
	if cfg, ok := a.ConfigFind(preferencesConfigName).(*preferencesConfig); ok {
		return cfg
	}
	log.Fatal("[app] can't find project config")
	return nil
}