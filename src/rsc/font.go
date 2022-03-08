package rsc

import _ "embed"

var (
	//go:embed font/Inter-Medium.ttf
	fontTTF []byte
	//go:embed font/icomoon.ttf
	fontIconsTTF []byte
)

func FontTTF() []byte {
	return fontTTF
}

func FontIconsTTF() []byte {
	return fontIconsTTF
}
