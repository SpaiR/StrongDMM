package cpvareditor

import "github.com/rs/zerolog/log"

const (
	configName    = "cpvareditor"
	configVersion = 1
)

type vareditorConfig struct {
	Version uint

	ShowModified bool
	ShowByType   bool
	ShowPins     bool
	ShowTmp      bool

	PinnedVarNames []string
}

func (vareditorConfig) Name() string {
	return configName
}

func (vareditorConfig) TryMigrate(_ map[string]any) (result map[string]any, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func (v *VarEditor) loadConfig() {
	v.app.ConfigRegister(&vareditorConfig{
		Version: configVersion,

		ShowPins: true,
	})
}

func (v *VarEditor) config() *vareditorConfig {
	if cfg, ok := v.app.ConfigFind(configName).(*vareditorConfig); ok {
		return cfg
	}
	log.Fatal().Msg("can't find config")
	return nil
}
