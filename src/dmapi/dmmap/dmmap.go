package dmmap

import (
	"log"

	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
)

var (
	WorldIconSize int

	/*
		Tiles should have at least one area and one turf.
		Those vars will store them to ensure that the tile has a proper content.
	*/
	BaseArea *dmmprefab.Prefab
	BaseTurf *dmmprefab.Prefab

	environment *dmenv.Dme
)

func Init(dme *dmenv.Dme) {
	environment = dme

	WorldIconSize = dme.Objects["/world"].Vars.IntV("icon_size", 32)

	baseAreaPath, _ := dme.Objects["/world"].Vars.Value("area")
	baseTurfPath, _ := dme.Objects["/world"].Vars.Value("turf")
	BaseArea = PrefabStorage.Initial(baseAreaPath)
	BaseTurf = PrefabStorage.Initial(baseTurfPath)

	log.Println("[dmmap] initialized with:", dme.RootFile)
	log.Println("[dmmap] base area:", baseAreaPath)
	log.Println("[dmmap] base turf:", baseTurfPath)
}
