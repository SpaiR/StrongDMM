package dmmap

import (
	"log"
	"path/filepath"

	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

// Dmm stores information about the map.
// Unlike the dmmdata.DmmData this information is needed mostly for the editor usages.
type Dmm struct {
	Name string
	Path DmmPath

	Tiles []*Tile

	MaxX, MaxY, MaxZ int

	Backup string
}

func (d *Dmm) HasTile(coord util.Point) bool {
	return coord.X > 0 && coord.Y > 0 && coord.Z > 0 && coord.X <= d.MaxX && coord.Y <= d.MaxY && coord.Z <= d.MaxZ
}

func (d *Dmm) GetTile(coord util.Point) *Tile {
	return d.Tiles[d.tileIndex(coord.X, coord.Y, coord.Z)]
}

// IsInstanceExist returns true if there is an instance with the provided ID on the map.
func (d *Dmm) IsInstanceExist(instanceId uint64) bool {
	for _, tile := range d.Tiles {
		for _, instance := range tile.instances {
			if instance.Id() == instanceId {
				return true
			}
		}
	}
	return false
}

func (d *Dmm) setTile(x, y, z int, tile *Tile) {
	d.Tiles[d.tileIndex(x, y, z)] = tile
}

// We store all tiles in the 1d-array, so we need to calculate tile position in it.
func (d *Dmm) tileIndex(x, y, z int) int {
	return d.MaxX*d.MaxY*(z-1) + d.MaxX*(y-1) + (x - 1)
}

func New(dme *dmenv.Dme, data *dmmdata.DmmData, backup string) *Dmm {
	dmm := Dmm{
		Name:  filepath.Base(data.Filepath),
		Path:  newDmmPath(dme.RootDir, data),
		Tiles: make([]*Tile, data.MaxX*data.MaxY*data.MaxZ),
		MaxX:  data.MaxX,
		MaxY:  data.MaxY,
		MaxZ:  data.MaxZ,

		Backup: backup,
	}

	for z := 1; z <= data.MaxZ; z++ {
		for y := 1; y <= data.MaxY; y++ {
			for x := 1; x <= data.MaxX; x++ {
				tile := Tile{Coord: util.Point{X: x, Y: y, Z: z}}

				for _, prefab := range data.Dictionary[data.Grid[tile.Coord]] {
					if obj, ok := dme.Objects[prefab.Path()]; ok {
						// Prefabs from the dmmdata don't know about environment objects.
						if !prefab.Vars().HasParent() {
							prefab.Vars().LinkParent(obj.Vars)
						}
						tile.InstancesAdd(PrefabStorage.Put(prefab))
					} else {
						log.Println("[dmmap] unknown prefab:", prefab.Path())
					}
				}

				dmm.setTile(x, y, z, &tile)
			}
		}
	}

	return &dmm
}

// PersistPrefabs persists all prefabs from instances on the current map.
func (d *Dmm) PersistPrefabs() {
	for _, tile := range d.Tiles {
		for _, instance := range tile.Instances() {
			PrefabStorage.Put(instance.Prefab())
		}
	}
}

type DmmPath struct {
	Readable string
	Absolute string
}

func newDmmPath(rootDir string, data *dmmdata.DmmData) DmmPath {
	readable, err := filepath.Rel(rootDir, data.Filepath)
	if err != nil {
		log.Println("[dmmap] unable to get relative path of the map:", data.Filepath)
		readable = data.Filepath
	}
	return DmmPath{
		Readable: readable,
		Absolute: data.Filepath,
	}
}
