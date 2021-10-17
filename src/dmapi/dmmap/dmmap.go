package dmmap

import (
	"log"

	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmvars"
)

var (
	WorldIconSize int

	// Tiles should have at least one area and one turf.
	// Those vars will store them to ensure that the tile has a proper content.
	baseArea, baseTurf *dmmdata.Prefab
)

func Init(dme *dmenv.Dme) {
	WorldIconSize = dme.Objects["/world"].Vars.IntV("icon_size", 32)

	baseAreaPath, _ := dme.Objects["/world"].Vars.Value("area")
	baseTurfPath, _ := dme.Objects["/world"].Vars.Value("turf")
	baseArea = PrefabStorage.Get(baseAreaPath, dmvars.FromParent(dme.Objects[baseAreaPath].Vars))
	baseTurf = PrefabStorage.Get(baseTurfPath, dmvars.FromParent(dme.Objects[baseTurfPath].Vars))

	log.Println("[dmmap] initialized with:", dme.RootFile)
	log.Println("[dmmap] base area:", baseAreaPath)
	log.Println("[dmmap] base turf:", baseTurfPath)
}
