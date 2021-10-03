package dmmap

import (
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

type Tile struct {
	Coord util.Point

	Content []dmminstance.Instance
}

func (t Tile) Copy() Tile {
	tileCopy := Tile{
		Coord:   t.Coord,
		Content: make([]dmminstance.Instance, len(t.Content)),
	}
	copy(tileCopy.Content, t.Content)
	return tileCopy
}
