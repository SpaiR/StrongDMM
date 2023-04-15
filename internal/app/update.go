package app

import (
	"runtime"

	"sdmm/internal/app/selfupdate"
	"sdmm/internal/env"
	"sdmm/internal/req"
	"sdmm/internal/util/slice"

	"github.com/rs/zerolog/log"
)

var remoteManifest selfupdate.Manifest

func (a *app) checkForUpdates() {
	a.checkForUpdatesV(false)
}

func (a *app) checkForUpdatesV(forceAvailable bool) {
	log.Print("checking for self updates...")

	manifest, err := selfupdate.FetchRemoteManifest()
	if err != nil {
		log.Printf("unable to fetch remote manifest: %v", err)
		return
	}

	remoteManifest = manifest

	if manifest.Version == env.Version {
		log.Print("application is up to date!")
		return
	}
	if slice.StrContains(a.config().UpdateIgnore, manifest.Version) && !forceAvailable {
		log.Print("ignoring update:", manifest.Version)
		return
	}

	log.Print("new update available:", manifest.Version)

	a.menu.SetUpdateAvailable(manifest.Version, manifest.Description)

	// Do force update only if we're using a concrete editor version.
	//goland:noinspection GoBoolExpressions
	if a.Prefs().Application.AutoUpdate && env.Version != env.Undefined {
		a.selfUpdate()
	}
}

func (a *app) selfUpdate() {
	a.menu.SetUpdating()

	var updateDownloadLink string

	switch runtime.GOOS {
	case "windows":
		updateDownloadLink = remoteManifest.DownloadLinks.Windows
	case "linux":
		updateDownloadLink = remoteManifest.DownloadLinks.Linux
	case "darwin":
		updateDownloadLink = remoteManifest.DownloadLinks.MacOS
	}

	log.Print("updating with:", updateDownloadLink)

	go func() {
		latestUpdate, err := req.Get(updateDownloadLink)
		if err != nil {
			log.Print("unable to get latest update:", err)
			a.menu.SetUpdateError()
			return
		}

		if err = selfupdate.Update(latestUpdate); err != nil {
			log.Print("unable to complete self update:", err)
			a.menu.SetUpdateError()
			return
		}

		a.menu.SetUpdated()

		log.Print("self update completed successfully!")
	}()
}
