package dmenv

import (
	"strongdmm/dm/dmvars"
)

type Object struct {
	env    *Dme
	parent *Object

	Vars           *dmvars.Variables
	Path           string
	DirectChildren []string
}
