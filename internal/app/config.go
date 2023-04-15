package app

import (
	"os"
	"path/filepath"
	"time"

	"sdmm/internal/app/config"

	"github.com/rs/zerolog/log"
)

func (a *app) ConfigRegister(cfg config.Config) {
	if a.configs == nil {
		a.configs = make(map[string]config.Config)
	}

	configFilePath := configFilePath(a.configDir, cfg.Name())

	log.Printf("registering config [%s] by path [%s]...", cfg.Name(), configFilePath)

	// Load a raw configuration data.
	rawCfg := make(map[string]any)
	err := config.LoadV(configFilePath, &rawCfg)
	if err != nil {
		log.Print("unable to load config:", cfg.Name()) // Highly likely doesn't exist.
	} else {
		// Try to do a migration. The result var will be a nil, if there is nothing to migrate.
		if result, migrated := cfg.TryMigrate(rawCfg); migrated {
			log.Print("migrated config:", configFilePath)
			config.SaveV(configFilePath, result)
		}

		// Load migrated (or not) data.
		err = config.Load(configFilePath, cfg)
		if err != nil {
			log.Fatal().Msgf("unable to load config: %s", configFilePath)
		}
	}

	a.configs[cfg.Name()] = cfg

	log.Print("config registered:", cfg.Name())
}

const backgroundSavePeriod = time.Minute * 3

func (a *app) runBackgroundConfigSave() {
	log.Printf("background configuration save every [%s]!", backgroundSavePeriod)
	go func() {
		for range time.Tick(backgroundSavePeriod) {
			a.configSave()
		}
	}()
}

func (a *app) configSave() {
	_ = os.MkdirAll(a.configDir, os.ModePerm)
	for _, cfg := range a.configs {
		a.configSaveV(cfg)
	}
}

func (a *app) configSaveV(cfg config.Config) {
	config.Save(configFilePath(a.configDir, cfg.Name()), cfg)
}

func (a *app) ConfigFind(name string) config.Config {
	if cfg, ok := a.configs[name]; ok {
		return cfg
	}
	log.Fatal().Msgf("unable to find config: %s", name)
	return nil
}

func configFilePath(dir, cfgName string) string {
	return filepath.FromSlash(dir + "/" + cfgName + ".json")
}
