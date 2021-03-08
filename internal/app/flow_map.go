package app

func (a *app) openMap(path string) {
	a.internalData.AddRecentMap(a.loadedEnvironment.RootFile, path)
}
