package data

import (
	"log"
	"path/filepath"

	"sdmm/util/slice"
)

const internalFileName = "internal.json"
const internalVersion = 1

type Internal struct {
	path string

	Version                 int
	RecentEnvironments      []string
	RecentMapsByEnvironment map[string][]string
}

func (i *Internal) AddRecentEnvironment(recentEnvironment string) {
	i.RecentEnvironments = slice.StrPushUnique(i.RecentEnvironments, recentEnvironment)
	log.Println("[data] added recent environment:", recentEnvironment)
}

func (i *Internal) ClearRecentEnvironments() {
	i.RecentEnvironments = nil
	log.Println("[data] cleared recent environments")
}

func (i *Internal) AddRecentMap(environment string, mapPath string) {
	maps := i.RecentMapsByEnvironment[environment]
	maps = slice.StrPushUnique(maps, mapPath)
	i.RecentMapsByEnvironment[environment] = maps
	log.Printf("[data] added recent map for environment [%s]: %s", environment, mapPath)
}

func (i *Internal) ClearRecentMaps(environment string) {
	i.RecentMapsByEnvironment[environment] = nil
	log.Printf("[data] cleared recent maps for environment [%s]", environment)
}

func (i *Internal) Save() {
	save(i.path, i)
	log.Println("[data] saved internal")
}

func LoadInternal(internalDir string) *Internal {
	path := filepath.FromSlash(internalDir + "/" + internalFileName)

	log.Println("[data] loading internal:", path)

	d := Internal{
		path: path,

		Version:                 internalVersion,
		RecentMapsByEnvironment: make(map[string][]string),
	}

	if err := read(path, &d); err != nil {
		log.Println("[data] unable to load internal:", err)
	} else {
		log.Println("[data] internal loaded:", d)
	}

	return &d
}
