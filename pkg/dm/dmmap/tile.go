package dmmap

import "github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"

type Tile struct {
	X, Y, Z int

	Content []*dmminstance.Instance
}

func (t Tile) Copy() Tile {
	tileCopy := Tile{
		X:       t.X,
		Y:       t.Y,
		Z:       t.Z,
		Content: make([]*dmminstance.Instance, len(t.Content)),
	}
	copy(tileCopy.Content, t.Content)
	return tileCopy
}
