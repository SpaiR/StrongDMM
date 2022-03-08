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

func AboutTxt(version, revision string) string {
	txt := strings.Replace(aboutTxt, "%VERSION%", version, 1)
	txt = strings.Replace(txt, "%REVISION%", revision, 1)
	return txt
}
