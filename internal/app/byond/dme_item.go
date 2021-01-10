package byond

import (
	"fmt"
	"strconv"
)

type DmeItem struct {
	env            *Dme
	Type           string
	Vars           map[string]*string
	DirectChildren []string
	Parent         *DmeItem
}

func (d *DmeItem) Var(name string) (string, error) {
	if value, ok := d.Vars[name]; ok {
		return *value, nil
	} else if d.Parent != nil {
		return d.Parent.Var(name)
	}
	return "", fmt.Errorf("unable to get var [%s] value", name)
}

func (d *DmeItem) VarText(name string) (string, error) {
	value, err := d.Var(name)
	if err != nil {
		return "", err
	}

	if len(value) > 1 {
		return value[1 : len(value)-1], nil
	}
	return value, nil
}

func (d *DmeItem) VarFloat(name string) (float32, error) {
	value, err := d.Var(name)
	if err != nil {
		return 0, err
	}

	v, err := strconv.ParseFloat(value, 32)
	if err != nil {
		return 0, err
	}

	return float32(v), nil
}

func (d *DmeItem) VarInt(name string) (int, error) {
	value, err := d.Var(name)
	if err != nil {
		return 0, err
	}

	v, err := strconv.ParseInt(value, 10, 32)
	if err != nil {
		return 0, err
	}

	return int(v), nil
}
