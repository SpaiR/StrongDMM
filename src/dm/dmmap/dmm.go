package dmmap

import (
	"log"
	"path/filepath"

	"sdmm/dm/dmenv"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmmap/dmminstance"
)

type Dmm struct {
	Name string
	Path DmmPath

	Tiles []*Tile

	MaxX, MaxY, MaxZ int
}

func (d *Dmm) GetTile(x, y, z int) *Tile {
	return d.Tiles[d.tileIndex(x, y, z)]
}

func (d *Dmm) SetTile(x, y, z int, tile *Tile) {
	d.Tiles[d.tileIndex(x, y, z)] = tile
}

// We store all tiles in the 1d-array, so we need to calculate tile position in it.
func (d *Dmm) tileIndex(x, y, z int) int {
	return d.MaxX*d.MaxY*(z-1) + d.MaxX*(y-1) + (x - 1)
}

func New(dme *dmenv.Dme, data *dmmdata.DmmData) *Dmm {
	dmm := Dmm{
		Name:  filepath.Base(data.Filepath),
		Path:  newDmmPath(dme, data),
		Tiles: make([]*Tile, data.MaxX*data.MaxY*data.MaxZ),
		MaxX:  data.MaxX,
		MaxY:  data.MaxY,
		MaxZ:  data.MaxZ,
	}

	for z := 1; z <= data.MaxZ; z++ {
		for y := 1; y <= data.MaxY; y++ {
			for x := 1; x <= data.MaxX; x++ {
				tile := Tile{X: x, Y: y, Z: z}
				coord := dmmdata.Coord{X: uint16(x), Y: uint16(y), Z: uint16(z)}
				for _, prefab := range data.Dictionary[data.Grid[coord]] {
					if obj, ok := dme.Objects[prefab.Path]; ok {
						prefab.Vars.SetParent(obj.Vars)
						tile.Content = append(tile.Content, dmminstance.Cache.Get(prefab.Path, prefab.Vars))
					} else {
						log.Println("[dmmap] unknown prefab:", prefab.Path)
					}
				}
				dmm.SetTile(x, y, z, &tile)
			}
		}
	}

	return &dmm
}

type DmmPath struct {
	Readable string
	Absolute string
}

func newDmmPath(dme *dmenv.Dme, data *dmmdata.DmmData) DmmPath {
	readable, err := filepath.Rel(dme.RootDir, data.Filepath)
	if err != nil {
		log.Println("[dmmap] unable to get relative path of the map:", data.Filepath)
		readable = data.Filepath
	}
	return DmmPath{
		Readable: readable,
		Absolute: data.Filepath,
	}
}
