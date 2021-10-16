package dmenv

import (
	"sdmm/dmapi/dmvars"
)

type Object struct {
	env    *Dme
	parent *Object

	Vars           *dmvars.Variables
	Path           string
	DirectChildren []string
}
