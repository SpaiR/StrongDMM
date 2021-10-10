package dmvars

import (
	"strconv"

	"sdmm/util/slice"
)

// Variables is a structure to store an instance (in the environment or on the map) variables.
// Those variables are stored as a "string2string" map and immutable by concept.
// It means that if you need to modify variables of an instance,
// then you need to create a new instance with modified variables.
// That restriction goes for the idea that instances by their nature are immutable as well.
//
// Variables do support an inheritance and value caching to avoid unnecessary value extraction.
type Variables struct {
	names  []string
	vars   map[string]*string
	parent *Variables

	// Caches are never invalidated, since Variables is an immutable structure and its state can't be modified.
	cacheText  map[string]string
	cacheFloat map[string]float32
	cacheInt   map[string]int
}

func FromParent(parent *Variables) *Variables {
	return &Variables{parent: parent}
}

// MutableVariables are used to provide a basic modification interface,
// without breaking of an immutability of Variables struct.
type MutableVariables struct {
	Variables
}

func (v *MutableVariables) Put(name string, value *string) {
	if v.vars == nil {
		v.vars = make(map[string]*string)
	}
	if !slice.StrContains(v.names, name) {
		v.names = append(v.names, name)
	}
	v.vars[name] = value
}

func (v *MutableVariables) ToImmutable() *Variables {
	return &Variables{
		names:      v.names,
		vars:       v.vars,
		parent:     v.parent,
		cacheText:  v.cacheText,
		cacheFloat: v.cacheFloat,
		cacheInt:   v.cacheInt,
	}
}

func (v *Variables) HasParent() bool {
	return v.parent != nil
}

func (v *Variables) LinkParent(parent *Variables) {
	if v.parent != nil {
		panic("Linking a parent to an occupied variables is prohibited!") // Just to ensure
	}
	v.parent = parent
}

func (v *Variables) Iterate() []string {
	return v.names
}

func (v *Variables) Len() int {
	return len(v.names)
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
	if v.cacheText == nil {
		v.cacheText = make(map[string]string)
	}
	if value, ok := v.cacheText[name]; ok {
		return value, true
	}

	var result string
	var contains bool

	value, ok := v.Value(name)
	if ok != true {
		result, contains = "", ok
	} else {
		if len(value) > 1 {
			result, contains = value[1:len(value)-1], true
		} else {
			result, contains = value, true
		}
	}
	v.cacheText[name] = result
	return result, contains
}

func (v *Variables) Float(name string) (float32, bool) {
	if v.cacheFloat == nil {
		v.cacheFloat = make(map[string]float32)
	}
	if value, ok := v.cacheFloat[name]; ok {
		return value, true
	}

	var result float32
	var contains bool

	value, ok := v.Value(name)
	if ok != true {
		result, contains = 0, ok
	} else {
		n, err := strconv.ParseFloat(value, 32)
		if err == nil {
			result, contains = float32(n), true
		}
	}
	v.cacheFloat[name] = result
	return result, contains
}

func (v *Variables) Int(name string) (int, bool) {
	if v.cacheInt == nil {
		v.cacheInt = make(map[string]int)
	}
	if value, ok := v.cacheInt[name]; ok {
		return value, true
	}

	var result int
	var contains bool

	value, ok := v.Value(name)
	if ok != true {
		result, contains = 0, ok
	} else {
		n, err := strconv.ParseInt(value, 10, 32)
		if err == nil {
			result, contains = int(n), true
		}
	}
	v.cacheInt[name] = result
	return result, contains
}
