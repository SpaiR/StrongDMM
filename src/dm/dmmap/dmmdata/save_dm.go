package dmmdata

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strings"

	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

// SaveDM writes DmmData to a file with the provided path.
func (d DmmData) SaveDM(path string) {
	f, err := os.Create(path)
	if err != nil {
		log.Printf("[dmmdata] unable to save as DM [%s]: %v", d, err)
		return
	}
	defer f.Close()

	w := bufio.NewWriter(f)
	write := func(str string) {
		_, _ = w.WriteString(str)
	}

	// Write instances.
	for _, key := range d.Keys() {
		write(toDMStr(key, d.Dictionary[key]))
		write(d.LineBreak)
	}

	// Write map grids.
	for z := 1; z <= d.MaxZ; z++ {
		write(d.LineBreak)
		write(fmt.Sprintf("(1,1,%d) = {\"%s", z, d.LineBreak))

		for y := d.MaxY; y >= 1; y-- {
			for x := 1; x <= d.MaxX; x++ {
				write(string(d.Grid[util.Point{X: x, Y: y, Z: z}]))
			}
			write(d.LineBreak)
		}

		write("\"}")
	}

	write(d.LineBreak)

	if err = w.Flush(); err != nil {
		log.Printf("[dmmdata] unable to write to [%s]: %v", path, err)
	}

	log.Printf("[dmmdata] [%s] saved in [DM] format to: %s", d, path)
}

func toDMStr(key Key, instances []dmminstance.Instance) string {
	sb := strings.Builder{}

	sb.WriteString(fmt.Sprintf("\"%s\" = (", key))

	for idx, instance := range instances {
		sb.WriteString(instance.Path)

		if instance.Vars.Len() > 0 {
			sb.WriteString("{")

			for idx, varName := range instance.Vars.Iterate() {
				varValue, _ := instance.Vars.Value(varName)

				sb.WriteString(varName)
				sb.WriteString(" = ")
				sb.WriteString(varValue)

				if idx != instance.Vars.Len()-1 {
					sb.WriteString("; ")
				}
			}

			sb.WriteString("}")
		}

		if idx != len(instances)-1 {
			sb.WriteString(",")
		}
	}

	sb.WriteString(")")

	return sb.String()
}
