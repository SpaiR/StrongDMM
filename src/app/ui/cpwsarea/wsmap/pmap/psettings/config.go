package psettings

import (
	"log"
	"os/user"
)

const (
	configName    = "psettings"
	configVersion = 1
)

type psettingsConfig struct {
	Version uint

	ScreenshotDir string
}

func (psettingsConfig) Name() string {
	return configName
}

func (psettingsConfig) TryMigrate(_ map[string]interface{}) (result map[string]interface{}, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func loadConfig(app App) *psettingsConfig {
	u, err := user.Current()
	if err != nil {
		log.Fatal("[psettings] unable to find user:", err)
	}

	cfg := &psettingsConfig{
		Version: configVersion,

		ScreenshotDir: u.HomeDir,
	}
	app.ConfigRegister(cfg)
	return cfg
}
