package dmmdata

import (
	"bufio"
	"fmt"
	"os"
	"strings"

	"sdmm/internal/util"

	"github.com/rs/zerolog/log"
)

// SaveDM writes DmmData in DM format to a file with the provided path.
func (d DmmData) SaveDM(path string) {
	log.Print("saving dmm data in format...")

	f, err := os.Create(path)
	if err != nil {
		log.Printf("unable to save as [%s]: %v", d, err)
		return
	}
	defer f.Close()

	w := bufio.NewWriter(f)
	write := func(str string) {
		_, _ = w.WriteString(str)
	}

	log.Print("writing prefabs...")

	for _, key := range d.Keys() {
		write(toDMStr(key, d.Dictionary[key]))
		write(d.LineBreak)
	}

	log.Print("writing grid...")

	for z := 1; z <= d.MaxZ; z++ {
		write(d.LineBreak)
		write(fmt.Sprintf("(1,1,%d) = {\"", z))
		write(d.LineBreak)

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
		log.Printf("unable to write to [%s]: %v", path, err)
	}

	log.Printf("[%s] saved in format to: %s", d, path)
}

func toDMStr(key Key, prefabs Prefabs) string {
	sb := strings.Builder{}

	sb.WriteString(fmt.Sprintf("\"%s\" = (", key))

	for idx, prefab := range prefabs {
		sb.WriteString(prefab.Path())

		if prefab.Vars().Len() > 0 {
			sb.WriteString("{")

			for idx, varName := range prefab.Vars().Iterate() {
				varValue, _ := prefab.Vars().Value(varName)

				sb.WriteString(varName)
				sb.WriteString(" = ")
				sb.WriteString(varValue)

				if idx != prefab.Vars().Len()-1 {
					sb.WriteString("; ")
				}
			}

			sb.WriteString("}")
		}

		if idx != len(prefabs)-1 {
			sb.WriteString(",")
		}
	}

	sb.WriteString(")")

	return sb.String()
}
