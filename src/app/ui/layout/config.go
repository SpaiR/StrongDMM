package layout

import "log"

const (
	configName    = "layout"
	configVersion = 1
	configState   = 1
)

type layoutConfig struct {
	Version uint
	State   uint // When different with the configState const - layout will be reset.
}

func (layoutConfig) Name() string {
	return configName
}

func (layoutConfig) TryMigrate(_ map[string]interface{}) (result map[string]interface{}, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func (l *Layout) loadLayoutConfig() {
	l.app.ConfigRegister(&layoutConfig{
		Version: configVersion,
		State:   configState,
	})
}

func (l *Layout) layoutConfig() *layoutConfig {
	if cfg, ok := l.app.ConfigFind(configName).(*layoutConfig); ok {
		return cfg
	}
	log.Fatal("[layout] can't find layout config")
	return nil
}
