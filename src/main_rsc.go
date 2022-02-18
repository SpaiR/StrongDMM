package main

import (
	_ "embed"
	"sdmm/rsc"
)

// Personally, I would like to store the CHANGELOG file in the root of the repo,
// but "embed" feature can't go to parent dirs. Storing the file in the rsc package already seems "to deep".
//go:embed CHANGELOG.md
var changelog string

func init() {
	rsc.Changelog = changelog
}
