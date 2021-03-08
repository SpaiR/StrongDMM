package dmmap

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/byond/dmenv"
)

type Dmm struct {
	Tiles map[Coord]*Tile

	MaxX, MaxY, MaxZ int
}

func (d *Dmm) GetTile(x, y, z int) *Tile {
	return d.Tiles[Coord{X: uint16(x), Y: uint16(y), Z: uint16(z)}]
}

func New(dme *dmenv.Dme, data *Data) *Dmm {
	dmm := Dmm{
		Tiles: make(map[Coord]*Tile),
		MaxX:  data.MaxX,
		MaxY:  data.MaxY,
		MaxZ:  data.MaxZ,
	}

	for z := 1; z <= data.MaxZ; z++ {
		for y := 1; y <= data.MaxY; y++ {
			for x := 1; x <= data.MaxX; x++ {
				coord := Coord{X: uint16(x), Y: uint16(y), Z: uint16(z)}
				tile := Tile{}
				for _, prefab := range data.Dictionary[data.Grid[coord]] {
					if obj, ok := dme.Objects[prefab.Path]; ok {
						prefab.Vars.SetParent(obj.Vars)
						tile.Content = append(tile.Content, GetInstance(prefab.Path, prefab.Vars))
					} else {
						log.Println("unknown prefab: ", prefab.Path)
					}
				}
				dmm.Tiles[coord] = &tile
			}
		}
	}

	return &dmm
}
