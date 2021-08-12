package data

import (
	"log"

	"sdmm/util/slice"
)

const internalFileName = "Internal.data"
const internalVersion = 1

type Internal struct {
	filepath string

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
	storeToFile(i.filepath,
		encode(i.Version),
		encode(i.RecentEnvironments),
		encode(i.RecentMapsByEnvironment),
	)
	log.Println("[data] saved internal")
}

func LoadInternal(internalDir string) *Internal {
	filepath := internalDir + "/" + internalFileName

	log.Println("[data] loading internal:", filepath)

	d := Internal{
		filepath: filepath,

		Version:                 internalVersion,
		RecentMapsByEnvironment: make(map[string][]string, 0),
	}

	data, err := readFromFile(filepath)
	if err != nil {
		log.Println("[data] unable to load internal data:", err)
		return &d
	}

	data.getInt(0, &d.Version)
	data.getStrSlice(1, &d.RecentEnvironments)
	data.getStrMapStrSlice(2, &d.RecentMapsByEnvironment)

	log.Println("[data] internal loaded:", d)

	return &d
}
