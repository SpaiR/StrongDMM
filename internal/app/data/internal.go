package data

import (
	"bytes"
	"encoding/gob"
	"io/ioutil"
	"log"
	"os"

	"github.com/SpaiR/strongdmm/pkg/util/slice"
)

const fileName = "Internal.data"

type Internal struct {
	filepath string

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
	i.encode()
}

func LoadInternal(internalDir string) *Internal {
	filepath := internalDir + "/" + fileName

	data := Internal{
		filepath: filepath,

		RecentMapsByEnvironment: make(map[string][]string, 0),
	}

	if err := data.decode(); err != nil {
		log.Println("unable to decode internal data: ", err)
	}

	return &data
}

func (i *Internal) encode() {
	buffer := bytes.Buffer{}
	encoder := gob.NewEncoder(&buffer)
	_ = encoder.Encode(i)
	_ = ioutil.WriteFile(i.filepath, buffer.Bytes(), os.ModePerm)
}

func (i *Internal) decode() error {
	content, _ := ioutil.ReadFile(i.filepath)
	buffer := bytes.Buffer{}
	buffer.Write(content)
	decoder := gob.NewDecoder(&buffer)
	return decoder.Decode(i)
}
