package app

import (
	"log"
	"os"
	"path/filepath"
	"time"

	"sdmm/internal/app/config"
)

func (a *app) ConfigRegister(cfg config.Config) {
	if a.configs == nil {
		a.configs = make(map[string]config.Config)
	}

	configFilePath := configFilePath(a.configDir, cfg.Name())

	log.Printf("[app] registering config [%s] by path [%s]...", cfg.Name(), configFilePath)

	// Load a raw configuration data.
	rawCfg := make(map[string]any)
	err := config.LoadV(configFilePath, &rawCfg)
	if err != nil {
		log.Println("[app] unable to load config:", cfg.Name()) // Highly likely doesn't exist.
	} else {
		// Try to do a migration. The result var will be a nil, if there is nothing to migrate.
		if result, migrated := cfg.TryMigrate(rawCfg); migrated {
			log.Println("[app] migrated config:", configFilePath)
			config.SaveV(configFilePath, result)
		}

		// Load migrated (or not) data.
		err = config.Load(configFilePath, cfg)
		if err != nil {
			log.Fatal("[app] unable to load config:", configFilePath)
		}
	}

	a.configs[cfg.Name()] = cfg

	log.Println("[app] config registered:", cfg.Name())
}

const backgroundSavePeriod = time.Minute * 3

func (a *app) runBackgroundConfigSave() {
	log.Printf("[app] background configuration save every [%s]!", backgroundSavePeriod)
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
	log.Fatalln("[app] unable to find config:", name)
	return nil
}

func configFilePath(dir, cfgName string) string {
	return filepath.FromSlash(dir + "/" + cfgName + ".json")
}
