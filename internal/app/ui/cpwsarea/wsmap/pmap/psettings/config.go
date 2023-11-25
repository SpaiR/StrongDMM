package psettings

import (
	"os/user"

	"github.com/rs/zerolog/log"
)

const (
	configName    = "psettings"
	configVersion = 1
)

type psettingsConfig struct {
	Version uint

	ScreenshotDir string

	InSelectionMode bool
	ToClipboardMode bool
}

func (psettingsConfig) Name() string {
	return configName
}

func (psettingsConfig) TryMigrate(_ map[string]any) (result map[string]any, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func loadConfig(app App) *psettingsConfig {
	u, err := user.Current()
	if err != nil {
		log.Fatal().Msgf("unable to find user: %v", err)
	}

	cfg := &psettingsConfig{
		Version: configVersion,

		ScreenshotDir:   u.HomeDir,
		ToClipboardMode: true,
	}
	app.ConfigRegister(cfg)
	return cfg
}
