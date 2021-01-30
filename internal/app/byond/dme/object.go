package dme

import (
	"strconv"
)

type Object struct {
	env            *Dme
	Type           string
	Vars           map[string]*string
	DirectChildren []string
	Parent         *Object
}

func (d *Object) Var(name string) (string, bool) {
	if d.Vars == nil && d.Parent != nil {
		return d.Parent.Var(name)
	} else if value, ok := d.Vars[name]; ok {
		if value == nil {
			return "", false
		}
		return *value, true
	} else if d.Parent != nil {
		return d.Parent.Var(name)
	}
	return "", false
}

func (d *Object) VarText(name string) (string, bool) {
	value, ok := d.Var(name)
	if ok != true {
		return "", ok
	}

	if len(value) > 1 {
		return value[1 : len(value)-1], true
	}
	return value, true
}

func (d *Object) VarFloat(name string) (float32, bool) {
	value, ok := d.Var(name)
	if ok != true {
		return 0, ok
	}

	v, err := strconv.ParseFloat(value, 32)
	if err != nil {
		return 0, false
	}

	return float32(v), true
}

func (d *Object) VarInt(name string) (int, bool) {
	value, ok := d.Var(name)
	if ok != true {
		return 0, ok
	}

	v, err := strconv.ParseInt(value, 10, 32)
	if err != nil {
		return 0, false
	}

	return int(v), true
}
