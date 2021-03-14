package dmmap

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/dm/dmenv"
	"github.com/SpaiR/strongdmm/internal/app/dm/dmmap/dmmdata"
	"github.com/SpaiR/strongdmm/internal/app/dm/dmmap/dmminstance"
)

type Dmm struct {
	Tiles map[dmmdata.Coord]*Tile

	MaxX, MaxY, MaxZ int
}

func (d *Dmm) GetTile(x, y, z int) *Tile {
	return d.Tiles[dmmdata.Coord{X: uint16(x), Y: uint16(y), Z: uint16(z)}]
}

func New(dme *dmenv.Dme, data *dmmdata.DmmData) *Dmm {
	dmm := Dmm{
		Tiles: make(map[dmmdata.Coord]*Tile),
		MaxX:  data.MaxX,
		MaxY:  data.MaxY,
		MaxZ:  data.MaxZ,
	}

	for z := 1; z <= data.MaxZ; z++ {
		for y := 1; y <= data.MaxY; y++ {
			for x := 1; x <= data.MaxX; x++ {
				coord := dmmdata.Coord{X: uint16(x), Y: uint16(y), Z: uint16(z)}
				tile := Tile{}
				for _, prefab := range data.Dictionary[data.Grid[coord]] {
					if obj, ok := dme.Objects[prefab.Path]; ok {
						prefab.Vars.SetParent(obj.Vars)
						tile.Content = append(tile.Content, dmminstance.Cache.Get(prefab.Path, prefab.Vars))
					} else {
						log.Println("[dmmap] unknown prefab:", prefab.Path)
					}
				}
				dmm.Tiles[coord] = &tile
			}
		}
	}

	return &dmm
}