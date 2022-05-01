package app

import (
	"log"
	"runtime"

	"sdmm/app/selfupdate"
	"sdmm/env"
	"sdmm/req"
	"sdmm/util/slice"
)

var remoteManifest selfupdate.Manifest

func (a *app) checkForUpdates() {
	a.checkForUpdatesV(false)
}

func (a *app) checkForUpdatesV(forceAvailable bool) {
	log.Println("[app] checking for self updates...")

	manifest, err := selfupdate.FetchRemoteManifest()
	if err != nil {
		log.Printf("[app] unable to fetch remote manifest: %v", err)
		return
	}

	remoteManifest = manifest

	if manifest.Version == env.Version {
		log.Println("[app] application is up to date!")
		return
	}
	if slice.StrContains(a.config().UpdateIgnore, manifest.Version) && !forceAvailable {
		log.Println("[app] ignore update:", manifest.Version)
		return
	}

	log.Println("[app] new update available:", manifest.Version)

	a.menu.SetUpdateAvailable(manifest.Version, manifest.Description)

	// Do force update only if we're using a concrete editor version.
	//goland:noinspection GoBoolExpressions
	if remoteManifest.ForceUpdate && env.Version != env.Undefined {
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

	log.Println("[app] updating with:", updateDownloadLink)

	go func() {
		latestUpdate, err := req.Get(updateDownloadLink)
		if err != nil {
			log.Println("[app] unable to get latest update:", err)
			a.menu.SetUpdateError()
			return
		}

		if err = selfupdate.Update(latestUpdate); err != nil {
			log.Println("[app] unable to complete self update:", err)
			a.menu.SetUpdateError()
			return
		}

		a.menu.SetUpdated()

		log.Println("[app] self update completed successfully!")
	}()
}
