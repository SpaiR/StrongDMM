package dmmap

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/byond/dmenv"
)

type Dmm struct {
	Tiles map[Coord]*Tile
}

func New(dme *dmenv.Dme, dmmData *Data) *Dmm {
	dmm := Dmm{
		Tiles: make(map[Coord]*Tile),
	}

	for z := 1; z <= dmmData.MaxZ; z++ {
		for y := 1; y < dmmData.MaxY; y++ {
			for x := 1; x < dmmData.MaxX; x++ {
				coord := Coord{X: uint16(x), Y: uint16(y), Z: uint16(z)}
				tile := Tile{}
				for _, prefab := range dmmData.Dictionary[dmmData.Grid[coord]] {
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
