package rsc

import _ "embed"

var (
	//go:embed font/Ruda-Bold.ttf
	fontTTF []byte
	//go:embed font/font-awesome-solid-900.ttf
	fontIconsTTF []byte
)

func FontTTF() []byte {
	return fontTTF
}

func FontIconsTTF() []byte {
	return fontIconsTTF
}