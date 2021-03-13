package data

import (
	"bytes"
	"encoding/gob"
	"log"
)

func encode(datum interface{}) datum {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)
	if err := enc.Encode(datum); err != nil {
		log.Println("[data] unable to encode data:", datum)
		return nil
	}
	return buf.Bytes()
}

func newDecoder(datum datum) *gob.Decoder {
	buffer := bytes.Buffer{}
	buffer.Write(datum)
	return gob.NewDecoder(&buffer)
}

func decodeStr(datum datum) string {
	var content string
	_ = newDecoder(datum).Decode(&content)
	return content
}

func decodeStrSlice(datum datum) []string {
	var content []string
	_ = newDecoder(datum).Decode(&content)
	return content
}

func decodeStrMapStrSlice(datum datum) map[string][]string {
	var content map[string][]string
	_ = newDecoder(datum).Decode(&content)
	return content
}

func decodeInt(datum datum) int {
	var content int
	_ = newDecoder(datum).Decode(&content)
	return content
}
