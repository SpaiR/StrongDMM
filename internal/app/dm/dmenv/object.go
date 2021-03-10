package dmenv

import "github.com/SpaiR/strongdmm/internal/app/dm/dmvars"

type Object struct {
	env    *Dme
	parent *Object

	Vars           *dmvars.Variables
	Path           string
	DirectChildren []string
}
