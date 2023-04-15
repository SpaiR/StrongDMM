package cpenvironment

import "github.com/rs/zerolog/log"

const (
	configName    = "cpenvironment"
	configVersion = 1
)

type cpenvironmentConfig struct {
	Version uint

	NodeScale int32
}

func (cpenvironmentConfig) Name() string {
	return configName
}

func (cpenvironmentConfig) TryMigrate(_ map[string]any) (result map[string]any, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func (e *Environment) loadConfig() {
	e.app.ConfigRegister(&cpenvironmentConfig{
		Version: configVersion,

		NodeScale: 100,
	})
}

func (e *Environment) config() *cpenvironmentConfig {
	if cfg, ok := e.app.ConfigFind(configName).(*cpenvironmentConfig); ok {
		return cfg
	}
	log.Fatal().Msg("can't find config")
	return nil
}
