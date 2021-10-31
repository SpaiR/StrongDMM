package prefs

import (
	"log"
	"path/filepath"

	"sdmm/app/data"
)

const (
	fileName      = "preferences.json"
	version  uint = 1
)

type Prefs struct {
	path string

	Version uint
	Scale   int
}

func (c *Prefs) Save() {
	data.Save(c.path, c)
	log.Println("[prefs] saved")
}

func Load(internalDir string) *Prefs {
	path := filepath.FromSlash(internalDir + "/" + fileName)

	d := Prefs{
		path: path,

		Version: version,
		Scale:   100,
	}

	if err := data.Load(path, &d); err != nil {
		log.Println("[prefs] unable to load:", err)
	} else {
		log.Println("[prefs] loaded:", d)
	}

	return &d
}
