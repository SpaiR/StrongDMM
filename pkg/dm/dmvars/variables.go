package dmvars

import (
	"strconv"

	"github.com/SpaiR/strongdmm/pkg/util/slice"
)

type Variables struct {
	names  []string
	vars   map[string]*string
	parent *Variables
}

func (v *Variables) SetParent(parentVars *Variables) {
	v.parent = parentVars
}

func (v *Variables) Iterate() []string {
	return v.names
}

func (v *Variables) Len() int {
	return len(v.names)
}

func (v *Variables) Put(name string, value *string) {
	if v.vars == nil {
		v.vars = make(map[string]*string)
	}
	if !slice.StrContains(v.names, name) {
		v.names = append(v.names, name)
	}
	v.vars[name] = value
}

func (v *Variables) Value(name string) (string, bool) {
	if v.vars == nil {
		if v.parent != nil {
			return v.parent.Value(name)
		}
	} else if value, ok := v.vars[name]; ok {
		if value == nil {
			return "", false
		}
		return *value, true
	} else if v.parent != nil {
		return v.parent.Value(name)
	}
	return "", false
}

func (v *Variables) Text(name string) (string, bool) {
	value, ok := v.Value(name)
	if ok != true {
		return "", ok
	}
	if len(value) > 1 {
		return value[1 : len(value)-1], true
	}
	return value, true
}

func (v *Variables) Float(name string) (float32, bool) {
	value, ok := v.Value(name)
	if ok != true {
		return 0, ok
	}
	n, err := strconv.ParseFloat(value, 32)
	if err != nil {
		return 0, false
	}
	return float32(n), true
}

func (v *Variables) Int(name string) (int, bool) {
	value, ok := v.Value(name)
	if ok != true {
		return 0, ok
	}
	n, err := strconv.ParseInt(value, 10, 32)
	if err != nil {
		return 0, false
	}
	return int(n), true
}
