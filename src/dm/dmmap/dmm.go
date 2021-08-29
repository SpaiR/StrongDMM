package dmmap

import (
	"log"
	"path/filepath"

	"sdmm/dm/dmenv"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

type Dmm struct {
	// Value is used mostly for stuff connected with the rendering.
	WorldIconSize int

	Name string
	Path DmmPath

	Tiles []*Tile

	MaxX, MaxY, MaxZ int
}

func (d *Dmm) GetTile(coord util.Point) *Tile {
	return d.Tiles[d.tileIndex(coord.X, coord.Y, coord.Z)]
}

func (d *Dmm) SetTile(x, y, z int, tile *Tile) {
	d.Tiles[d.tileIndex(x, y, z)] = tile
}

// We store all tiles in the 1d-array, so we need to calculate tile position in it.
func (d *Dmm) tileIndex(x, y, z int) int {
	return d.MaxX*d.MaxY*(z-1) + d.MaxX*(y-1) + (x - 1)
}

func New(dme *dmenv.Dme, data *dmmdata.DmmData) *Dmm {
	worldIconSize, _ := dme.Objects["/world"].Vars.Int("icon_size")

	dmm := Dmm{
		WorldIconSize: worldIconSize,

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
				tile := Tile{Coord: util.Point{X: x, Y: y, Z: z}}
				for _, instance := range data.Dictionary[data.Grid[tile.Coord]] {
					if obj, ok := dme.Objects[instance.Path]; ok {
						instance.Vars.SetParent(obj.Vars) // Instances from the dmmdata don't know about env objects.
						tile.Content = append(tile.Content, dmminstance.Cache.Put(instance))
					} else {
						log.Println("[dmmap] unknown instance:", instance.Path)
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
