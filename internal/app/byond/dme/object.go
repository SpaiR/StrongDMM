package dme

import "github.com/SpaiR/strongdmm/internal/app/byond/vars"

type Object struct {
	env    *Dme
	parent *Object

	Vars           *vars.Variables
	Path           string
	DirectChildren []string
}
