package dmmap

import (
	"log"
	"path/filepath"

	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"

	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmmap/dmmdata"
	"sdmm/internal/util"
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

func (d *Dmm) Copy() Dmm {
	dmm := Dmm{}
	dmm.Name = d.Name
	dmm.Path = d.Path
	dmm.MaxX = d.MaxX
	dmm.MaxY = d.MaxY
	dmm.MaxZ = d.MaxZ
	dmm.Backup = d.Backup

	// Do a deep copy for tiles
	dmm.Tiles = make([]*Tile, 0, len(d.Tiles))
	for _, t := range d.Tiles {
		tile := t.Copy()
		dmm.Tiles = append(dmm.Tiles, &tile)
	}

	return dmm
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

func (d *Dmm) SetMapSize(maxX, maxY, maxZ int) {
	newTiles := make([]*Tile, maxX*maxY*maxZ)

	for z := 1; z <= maxZ; z++ {
		for y := 1; y <= maxY; y++ {
			for x := 1; x <= maxX; x++ {
				coord := util.Point{X: x, Y: y, Z: z}
				tileIndex := tileIndex(maxX, maxY, x, y, z)
				if d.HasTile(coord) {
					newTiles[tileIndex] = d.GetTile(util.Point{X: x, Y: y, Z: z})
				} else {
					// Fill an empty tile with basic prefabs.
					newTiles[tileIndex] = &Tile{
						Coord:     coord,
						instances: InstancesFromPrefabs(coord, dmmdata.Prefabs{BaseTurf, BaseArea}),
					}
				}
			}
		}
	}

	d.Tiles = newTiles
	d.MaxX = maxX
	d.MaxY = maxY
	d.MaxZ = maxZ
}

func (d *Dmm) setTile(x, y, z int, tile *Tile) {
	d.Tiles[d.tileIndex(x, y, z)] = tile
}

func (d *Dmm) tileIndex(x, y, z int) int {
	return tileIndex(d.MaxX, d.MaxY, x, y, z)
}

// We store all tiles in the 1d-array, so we need to calculate tile position in it.
func tileIndex(maxX, maxY, x, y, z int) int {
	return maxX*maxY*(z-1) + maxX*(y-1) + (x - 1)
}

func New(dme *dmenv.Dme, data *dmmdata.DmmData, backup string) (dmm *Dmm, unknownPrefabs map[string]*dmmprefab.Prefab) {
	unknownPrefabs = make(map[string]*dmmprefab.Prefab)
	dmm = &Dmm{
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
						unknownPrefabs[prefab.Path()] = prefab
					}
				}

				dmm.setTile(x, y, z, &tile)
			}
		}
	}

	return dmm, unknownPrefabs
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
