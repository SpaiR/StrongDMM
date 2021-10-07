package data

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
)

func Save(filepath string, data interface{}) {
	log.Println("[data] saving:", filepath)
	f, err := os.Create(filepath)
	if err != nil {
		log.Println("[data] unable to create file by path:", filepath)
		return
	}
	defer f.Close()

	if j, err := json.Marshal(data); err == nil {
		_, _ = f.Write(j)
	} else {
		log.Println("[data] unable to save data by path:", filepath)
	}
}

func Load(filepath string, data interface{}) error {
	log.Println("[data] reading:", filepath)
	f, err := os.Open(filepath)
	if err != nil {
		return err
	}
	defer f.Close()

	var j []byte
	if j, err = ioutil.ReadFile(filepath); err == nil {
		err = json.Unmarshal(j, data)
	}

	return err
}
