package data

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/util/slice"
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
}

func (i *Internal) ClearRecentEnvironments() {
	i.RecentEnvironments = nil
}

func (i *Internal) AddRecentMap(currentEnvironment string, mapPath string) {
	maps := i.RecentMapsByEnvironment[currentEnvironment]
	maps = slice.StrPushUnique(maps, mapPath)
	i.RecentMapsByEnvironment[currentEnvironment] = maps
}

func (i *Internal) ClearRecentMaps(currentEnvironment string) {
	i.RecentMapsByEnvironment[currentEnvironment] = nil
}

func (i *Internal) Save() {
	storeToFile(i.filepath,
		encode(i.Version),
		encode(i.RecentEnvironments),
		encode(i.RecentMapsByEnvironment),
	)
}

func LoadInternal(internalDir string) *Internal {
	filepath := internalDir + "/" + internalFileName

	d := Internal{
		filepath: filepath,

		Version:                 internalVersion,
		RecentMapsByEnvironment: make(map[string][]string, 0),
	}

	data, err := readFromFile(filepath)
	if err != nil {
		log.Println("unable to load internal data: ", err)
		return &d
	}

	data.getInt(0, &d.Version)
	data.getStrSlice(1, &d.RecentEnvironments)
	data.getStrMapStrSlice(2, &d.RecentMapsByEnvironment)

	return &d
}
