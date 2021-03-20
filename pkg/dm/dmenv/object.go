package dmenv

import "github.com/SpaiR/strongdmm/pkg/dm/dmvars"

type Object struct {
	env    *Dme
	parent *Object

	Vars           *dmvars.Variables
	Path           string
	DirectChildren []string
}
