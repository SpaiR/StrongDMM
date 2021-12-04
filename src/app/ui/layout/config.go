package layout

import "log"

const (
	configName    = "layout"
	configVersion = 1
)

type layoutConfig struct {
	Version uint
}

func (layoutConfig) Name() string {
	return configName
}

func (layoutConfig) TryMigrate(rawCfg map[string]interface{}) (result map[string]interface{}, migrated bool) {
	rawVersion := uint(rawCfg["Version"].(float64))

	// Any change of the layout config will result in a full layout reset.
	versionUpdated = rawVersion != configVersion
	migrated = versionUpdated

	if versionUpdated {
		rawCfg["Version"] = configVersion
	}

	return rawCfg, migrated
}

func (l *Layout) loadLayoutConfig() {
	l.app.ConfigRegister(&layoutConfig{
		Version: configVersion,
	})
}

func (l *Layout) layoutConfig() *layoutConfig {
	if cfg, ok := l.app.ConfigFind(configName).(*layoutConfig); ok {
		return cfg
	}
	log.Fatal("[layout] can't find layout config")
	return nil
}
