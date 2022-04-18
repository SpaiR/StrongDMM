package app

import "log"

const (
	configName    = "app"
	configVersion = 1
)

type appConfig struct {
	Version uint

	UpdateIgnore []string
}

func (appConfig) Name() string {
	return configName
}

func (appConfig) TryMigrate(_ map[string]any) (result map[string]any, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func (a *app) loadConfig() {
	a.ConfigRegister(&appConfig{
		Version: configVersion,
	})
}

func (a *app) config() *appConfig {
	if cfg, ok := a.ConfigFind(configName).(*appConfig); ok {
		return cfg
	}
	log.Fatal("[app] can't find config")
	return nil
}
