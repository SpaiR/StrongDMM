package dmmdata

import (
	"fmt"
	"os"
	"sort"

	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

type DmmData struct {
	Filepath string

	IsTgm     bool
	LineBreak string

	KeyLength        int
	MaxX, MaxY, MaxZ int

	Dictionary map[Key][]dmminstance.Instance
	Grid       map[util.Point]Key
}

func (d DmmData) Keys() []Key {
	keys := make([]Key, 0, len(d.Dictionary))
	for key := range d.Dictionary {
		keys = append(keys, key)
	}

	sort.Slice(keys, func(i, j int) bool {
		return keys[i].ToNum() < keys[j].ToNum()
	})

	return keys
}

func (d DmmData) String() string {
	var winLineBreak bool
	if d.LineBreak == "\r\n" {
		winLineBreak = true
	}
	return fmt.Sprintf(
		"Filepath: %s, IsTgm: %t, WinLineBreak: %v, KeyLength: %d, MaxX: %d, MaxY: %d, MaxZ: %d",
		d.Filepath, d.IsTgm, winLineBreak, d.KeyLength, d.MaxX, d.MaxY, d.MaxZ)
}

func New(path string) (*DmmData, error) {
	file, err := os.Open(path)
	if err != nil {
		return nil, err
	}
	defer file.Close()
	return parse(file)
}
