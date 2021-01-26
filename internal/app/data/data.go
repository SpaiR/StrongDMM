package data

import (
	"bytes"
	"encoding/base64"
	"encoding/gob"
	"io/ioutil"
	"log"
	"os"

	"github.com/SpaiR/strongdmm/pkg/util/slice"
)

const fileName = "Internal.data"

type InternalData struct {
	filepath string

	RecentEnvironments      []string
	RecentMapsByEnvironment map[string][]string
}

func (i *InternalData) AddRecentEnvironment(recentEnvironment string) {
	i.RecentEnvironments = slice.StrPushUnique(i.RecentEnvironments, recentEnvironment)
}

func (i *InternalData) ClearRecentEnvironments() {
	i.RecentEnvironments = nil
}

func (i *InternalData) AddRecentMap(currentEnvironment string, mapPath string) {
	maps := i.RecentMapsByEnvironment[currentEnvironment]
	maps = slice.StrPushUnique(maps, mapPath)
	i.RecentMapsByEnvironment[currentEnvironment] = maps
}

func (i *InternalData) ClearRecentMaps(currentEnvironment string) {
	i.RecentMapsByEnvironment[currentEnvironment] = nil
}

func (i *InternalData) Save() {
	i.encode()
}

func Load(internalDir string) *InternalData {
	filepath := internalDir + "/" + fileName

	data := InternalData{
		filepath: filepath,

		RecentMapsByEnvironment: make(map[string][]string, 0),
	}

	if err := data.decode(); err != nil {
		log.Println("unable to decode internal data: ", err)
	}

	return &data
}

func (i *InternalData) encode() {
	buffer := bytes.Buffer{}
	encoder := gob.NewEncoder(&buffer)
	_ = encoder.Encode(i)
	content := base64.StdEncoding.EncodeToString(buffer.Bytes())
	_ = ioutil.WriteFile(i.filepath, []byte(content), os.ModePerm)
}

func (i *InternalData) decode() error {
	content, _ := ioutil.ReadFile(i.filepath)
	content, _ = base64.StdEncoding.DecodeString(string(content))
	buffer := bytes.Buffer{}
	buffer.Write(content)
	decoder := gob.NewDecoder(&buffer)
	return decoder.Decode(i)
}
