package config

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
)

type Config interface {
	Name() string
	TryMigrate(rawCfg map[string]any) (result map[string]any, migrated bool)
}

func Save(filepath string, cfg Config) {
	SaveV(filepath, cfg)
}

func SaveV(filepath string, cfg any) {
	log.Println("[config] saving:", filepath)
	f, err := os.Create(filepath)
	if err != nil {
		log.Println("[config] unable to create file by path:", filepath)
		return
	}
	defer f.Close()

	if j, err := json.Marshal(cfg); err == nil {
		_, _ = f.Write(j)
	} else {
		log.Println("[config] unable to save data by path:", filepath)
	}
}

func Load(filepath string, cfg Config) error {
	return LoadV(filepath, cfg)
}

func LoadV(filepath string, cfg any) error {
	log.Println("[config] reading:", filepath)
	f, err := os.Open(filepath)
	if err != nil {
		return err
	}
	defer f.Close()

	var j []byte
	if j, err = ioutil.ReadFile(filepath); err == nil {
		err = json.Unmarshal(j, cfg)
	}

	return err
}
