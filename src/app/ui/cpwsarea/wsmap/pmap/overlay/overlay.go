package overlay

import (
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

type FlickArea struct {
	Time float64
	Area util.Bounds
}

type FlickInstance struct {
	Time     float64
	Instance *dmminstance.Instance
}
