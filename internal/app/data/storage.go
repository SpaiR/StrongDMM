package data

import (
	"bufio"
	"log"
	"os"
)

type data [][]byte
type datum []byte

func (d data) doGet(idx int, do func(datum)) {
	if len(d) > idx {
		do(d[idx])
	}
}

func (d data) getInt(idx int, to *int) {
	d.doGet(idx, func(d datum) {
		*to = decodeInt(d)
	})
}

func (d data) getStrSlice(idx int, to *[]string) {
	d.doGet(idx, func(d datum) {
		*to = decodeStrSlice(d)
	})
}

func (d data) getStrMapStrSlice(idx int, to *map[string][]string) {
	d.doGet(idx, func(d datum) {
		*to = decodeStrMapStrSlice(d)
	})
}

func storeToFile(filepath string, data ...datum) {
	f, err := os.Create(filepath)
	if err != nil {
		log.Println("unable to store data by path: ", filepath)
		return
	}
	defer f.Close()

	for _, datum := range data {
		_, _ = f.Write(datum)
		_, _ = f.WriteString("\n")
	}
}

func readFromFile(filepath string) (data, error) {
	f, err := os.Open(filepath)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	var rows data
	w := bufio.NewReader(f)
	line, _, err := w.ReadLine()
	for err == nil {
		rows = append(rows, line)
		line, _, err = w.ReadLine()
	}

	return rows, nil
}
