package main

import (
	_ "embed"
	"sdmm/internal/rsc"
)

// File is meant to be used as an "embeder" of application assets.

var (
	//go:embed CHANGELOG.md
	changelogMd string
)

func init() {
	rsc.ChangelogMd = changelogMd
}
