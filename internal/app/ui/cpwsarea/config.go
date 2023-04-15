package cpwsarea

import "github.com/rs/zerolog/log"

const (
	configName    = "cpwsarea"
	configVersion = 1
)

type cpwsareaConfig struct {
	Version uint

	LastChangelogHash uint64
}

func (cpwsareaConfig) Name() string {
	return configName
}

func (cpwsareaConfig) TryMigrate(_ map[string]any) (result map[string]any, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func (w *WsArea) loadConfig() {
	w.app.ConfigRegister(&cpwsareaConfig{
		Version: configVersion,
	})
}

func (w *WsArea) config() *cpwsareaConfig {
	if cfg, ok := w.app.ConfigFind(configName).(*cpwsareaConfig); ok {
		return cfg
	}
	log.Fatal().Msg("can't find config")
	return nil
}
