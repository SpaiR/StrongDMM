package config

import (
	"encoding/json"
	"os"

	"github.com/rs/zerolog/log"
)

type Config interface {
	Name() string
	TryMigrate(rawCfg map[string]any) (result map[string]any, migrated bool)
}

func Save(filepath string, cfg Config) {
	SaveV(filepath, cfg)
}

func SaveV(filepath string, cfg any) {
	log.Print("saving:", filepath)
	f, err := os.Create(filepath)
	if err != nil {
		log.Print("unable to create file by path:", filepath)
		return
	}
	defer f.Close()

	if j, err := json.Marshal(cfg); err == nil {
		_, _ = f.Write(j)
	} else {
		log.Print("unable to save data by path:", filepath)
	}
}

func Load(filepath string, cfg Config) error {
	return LoadV(filepath, cfg)
}

func LoadV(filepath string, cfg any) error {
	log.Print("reading:", filepath)
	f, err := os.Open(filepath)
	if err != nil {
		return err
	}
	defer f.Close()

	var j []byte
	if j, err = os.ReadFile(filepath); err == nil {
		err = json.Unmarshal(j, cfg)
	}

	return err
}
