package config

import (
	"log"
	"path/filepath"

	"sdmm/app/data"
	"sdmm/util/slice"
)

const (
	fileName      = "config.json"
	version  uint = 1
)

type Config struct {
	path string

	Version                 uint
	LayoutVersion           uint
	RecentEnvironments      []string
	RecentMapsByEnvironment map[string][]string
}

func (c *Config) AddRecentEnvironment(recentEnvironment string) {
	c.RecentEnvironments = slice.StrPushUnique(c.RecentEnvironments, recentEnvironment)
	log.Println("[config] added recent environment:", recentEnvironment)
}

func (c *Config) ClearRecentEnvironments() {
	c.RecentEnvironments = nil
	log.Println("[config] cleared recent environments")
}

func (c *Config) AddRecentMap(environment string, mapPath string) {
	maps := c.RecentMapsByEnvironment[environment]
	maps = slice.StrPushUnique(maps, mapPath)
	c.RecentMapsByEnvironment[environment] = maps
	log.Printf("[config] added recent map for environment [%s]: %s", environment, mapPath)
}

func (c *Config) ClearRecentMaps(environment string) {
	c.RecentMapsByEnvironment[environment] = nil
	log.Printf("[config] cleared recent maps for environment [%s]", environment)
}

func (c *Config) Save() {
	data.Save(c.path, c)
	log.Println("[config] saved")
}

func Load(internalDir string) *Config {
	path := filepath.FromSlash(internalDir + "/" + fileName)

	d := Config{
		path: path,

		Version: version,

		RecentMapsByEnvironment: make(map[string][]string),
	}

	if err := data.Load(path, &d); err != nil {
		log.Println("[config] unable to load:", err)
	} else {
		log.Println("[config] loaded:", d)
	}

	return &d
}
