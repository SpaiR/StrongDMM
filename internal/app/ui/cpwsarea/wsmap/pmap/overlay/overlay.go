package overlay

import (
	"sdmm/internal/dmapi/dmmap/dmminstance"
	"sdmm/internal/util"
)

type FlickArea struct {
	Time float64
	Area util.Bounds
}

type FlickInstance struct {
	Time     float64
	Instance *dmminstance.Instance
}
