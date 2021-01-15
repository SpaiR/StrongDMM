package byond

import (
	"strconv"
)

type DmeItem struct {
	env            *Dme
	Type           string
	Vars           map[string]*string
	DirectChildren []string
	Parent         *DmeItem
}

func (d *DmeItem) Var(name string) (string, bool) {
	if value, ok := d.Vars[name]; ok {
		if value == nil {
			return "", false
		}
		return *value, true
	} else if d.Parent != nil {
		return d.Parent.Var(name)
	}
	return "", false
}

func (d *DmeItem) VarText(name string) (string, bool) {
	value, ok := d.Var(name)
	if ok != true {
		return "", ok
	}

	if len(value) > 1 {
		return value[1 : len(value)-1], true
	}
	return value, true
}

func (d *DmeItem) VarFloat(name string) (float32, bool) {
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

func (d *DmeItem) VarInt(name string) (int, bool) {
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
