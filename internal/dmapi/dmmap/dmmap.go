package dmmap

import (
	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"

	"github.com/rs/zerolog/log"
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

	log.Print("initialized with:", dme.RootFile)
	log.Print("base area:", baseAreaPath)
	log.Print("base turf:", baseTurfPath)
}

func Free() {
	environment = nil
	WorldIconSize = 0
	BaseArea = nil
	BaseTurf = nil
}
