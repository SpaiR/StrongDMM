package dmenv

import (
	"sdmm/internal/dmapi/dmvars"
)

type VarFlags struct {
	Tmp    bool
	Const  bool
	Static bool
}

func (vf VarFlags) Any() bool {
	return vf.Tmp || vf.Const || vf.Static
}

func (vf VarFlags) ReadOnly() bool {
	return vf.Const || vf.Static
}

type Object struct {
	env    *Dme
	parent *Object

	Vars           *dmvars.Variables
	VarFlags       map[string]VarFlags
	Path           string
	DirectChildren []string
}

func (o *Object) Parent() *Object {
	return o.parent
}

func (o *Object) Flags(varName string) VarFlags {
	for o != nil {
		if value, ok := o.VarFlags[varName]; ok {
			return value
		}
		o = o.parent
	}
	return VarFlags{}
}
