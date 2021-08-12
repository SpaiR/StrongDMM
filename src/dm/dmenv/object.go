package dmenv

import (
	"sdmm/dm/dmvars"
)

type Object struct {
	env    *Dme
	parent *Object

	Vars           *dmvars.Variables
	Path           string
	DirectChildren []string
}
