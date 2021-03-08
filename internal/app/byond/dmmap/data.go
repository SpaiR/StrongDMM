package dmmap

import (
	"fmt"
	"os"

	"github.com/SpaiR/strongdmm/internal/app/byond/dmvars"
)

type Key string

type Coord struct {
	X, Y, Z uint16
}

func (c Coord) String() string {
	return fmt.Sprintf("X:%d, Y:%d, Z:%d", c.X, c.Y, c.Z)
}

type Data struct {
	Filepath              string
	IsTgm, IsWinLineBreak bool

	KeyLength        int
	MaxX, MaxY, MaxZ int

	Dictionary map[Key][]Prefab
	Grid       map[Coord]Key
}

type Prefab struct {
	Path string
	Vars *dmvars.Variables
}

func ParseData(path string) (*Data, error) {
	file, err := os.Open(path)
	if err != nil {
		return nil, err
	}
	defer file.Close()
	return parseData(file)
}
