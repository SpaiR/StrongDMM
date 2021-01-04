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

type InternalData struct {
	filepath string

	RecentEnvironments []string
}

func (i *InternalData) AddRecentEnvironment(recentEnvironment string) {
	i.RecentEnvironments = slice.PushUniqueString(i.RecentEnvironments, recentEnvironment)
}

func (i *InternalData) Save() {
	i.encode()
}

func Load(internalDir string) *InternalData {
	filepath := internalDir + "/" + fileName

	data := InternalData{
		filepath: filepath,
	}

	if _, err := os.Stat(filepath); os.IsExist(err) {
		if err := data.decode(); err != nil {
			log.Println("unable to decode internal data: ", err)
		}
	}

	return &data
}

func (i *InternalData) encode() {
	buffer := bytes.Buffer{}
	encoder := gob.NewEncoder(&buffer)
	_ = encoder.Encode(i)
	_ = ioutil.WriteFile(i.filepath, buffer.Bytes(), os.ModePerm)
}

func (i *InternalData) decode() error {
	content, _ := ioutil.ReadFile(i.filepath)
	buffer := bytes.Buffer{}
	buffer.Write(content)
	decoder := gob.NewDecoder(&buffer)
	return decoder.Decode(i)
}
