package rsc

import (
	_ "embed"
	"strings"
)

var (
	//go:embed txt/about.txt
	aboutTxt string
	//go:embed txt/support.txt
	SupportTxt string
	//go:embed txt/changelog-header.txt
	ChangelogHeaderTxt string

	ChangelogMd string
)

func AboutTxt(version string) string {
	return strings.Replace(aboutTxt, "%VERSION%", version, 1)
}
