package cpsearch

import "log"

const (
	configName    = "cpsearch"
	configVersion = 1
)

type cpSearchConfig struct {
	Version uint
	Columns uint
}

func (cpSearchConfig) Name() string {
	return configName
}

func (cfg *cpSearchConfig) TryMigrate(_ map[string]interface{}) (result map[string]interface{}, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func (s *Search) loadConfig() {
	s.app.ConfigRegister(&cpSearchConfig{
		Version: configVersion,
		Columns: 2, // default columns count
	})
}

func (s *Search) config() *cpSearchConfig {
	if cfg, ok := s.app.ConfigFind(configName).(*cpSearchConfig); ok {
		return cfg
	}
	log.Fatal("[cpsearch] can't find config")
	return nil
}
