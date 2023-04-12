package selfupdate

import (
	"encoding/json"
	"fmt"
	"strings"

	"sdmm/internal/env"
	"sdmm/internal/req"
)

type Manifest struct {
	Name          string        `json:"name"`
	Version       string        `json:"version"`
	Description   string        `json:"description"`
	DownloadLinks DownloadLinks `json:"downloadLinks"`
}

type DownloadLinks struct {
	Windows string `json:"windows"`
	Linux   string `json:"linux"`
	MacOS   string `json:"macOS"`
}

const (
	placeholderVersion = "%VERSION%"
)

func ParseManifest(data []byte) (manifest Manifest, err error) {
	if err = json.Unmarshal(data, &manifest); err != nil {
		return Manifest{}, err
	}

	// Replace version placeholder with the actual value.
	replaceVersionPlaceholder(&manifest.DownloadLinks.Windows, manifest.Version)
	replaceVersionPlaceholder(&manifest.DownloadLinks.Linux, manifest.Version)
	replaceVersionPlaceholder(&manifest.DownloadLinks.MacOS, manifest.Version)

	return manifest, nil
}

func replaceVersionPlaceholder(url *string, version string) {
	*url = strings.ReplaceAll(*url, placeholderVersion, version)
}

func FetchRemoteManifest() (Manifest, error) {
	if manifestData, err := req.Get(env.Manifest); err == nil {
		return ParseManifest(manifestData)
	} else {
		return Manifest{}, fmt.Errorf("unable to get manifest data: %v", err)
	}
}
