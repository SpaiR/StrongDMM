package dmmdata

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strings"

	"sdmm/util"
)

// SaveTGM writes DmmData in TGM format to a file with the provided path.
func (d DmmData) SaveTGM(path string) {
	log.Println("[dmmdata] saving dmm data in [TGM] format...")

	f, err := os.Create(path)
	if err != nil {
		log.Printf("[dmmdata] unable to save as [TGM] [%s]: %v", d, err)
		return
	}
	defer f.Close()

	w := bufio.NewWriter(f)
	writeln := func(str ...string) {
		for _, s := range str {
			_, _ = w.WriteString(s)
		}
		_, _ = w.WriteString(d.LineBreak)
	}

	// Write TGM header
	// yeah, yeah, dmm2tgm.py, sure...
	writeln("//MAP CONVERTED BY dmm2tgm.py THIS HEADER COMMENT PREVENTS RECONVERSION, DO NOT REMOVE")

	log.Println("[dmmdata] writing instances...")

	// Write instances.
	for _, key := range d.Keys() {
		writeln(toTGMStr(key, d.Dictionary[key], d.LineBreak))
	}

	log.Println("[dmmdata] writing grid...")

	// Write map grids.
	for z := 1; z <= d.MaxZ; z++ {
		writeln()

		for x := 1; x <= d.MaxX; x++ {
			writeln(fmt.Sprintf("(%d,1,%d) = {\"", x, z))

			for y := d.MaxY; y >= 1; y-- {
				writeln(string(d.Grid[util.Point{X: x, Y: y, Z: z}]))
			}

			writeln("\"}")
		}
	}

	if err = w.Flush(); err != nil {
		log.Printf("[dmmdata] unable to write to [%s]: %v", path, err)
	}

	log.Printf("[dmmdata] [%s] saved in [TGM] format to: %s", d, path)
}

func toTGMStr(key Key, content Content, lineBreak string) string {
	sb := strings.Builder{}

	sb.WriteString(fmt.Sprintf("\"%s\" = (", key))
	sb.WriteString(lineBreak)

	for idx, instance := range content {
		sb.WriteString(instance.Path())

		if instance.Vars().Len() > 0 {
			sb.WriteString("{")
			sb.WriteString(lineBreak)

			for idx, varName := range instance.Vars().Iterate() {
				varValue, _ := instance.Vars().Value(varName)

				sb.WriteString("\t")
				sb.WriteString(varName)
				sb.WriteString(" = ")
				sb.WriteString(varValue)

				if idx != instance.Vars().Len()-1 {
					sb.WriteString(";")
				}

				sb.WriteString(lineBreak)
			}

			sb.WriteString("\t}")
		}

		if idx != len(content)-1 {
			sb.WriteString(",")
			sb.WriteString(lineBreak)
		}
	}

	sb.WriteString(")")

	return sb.String()
}
