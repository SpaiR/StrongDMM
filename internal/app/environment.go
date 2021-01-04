package app

func (a *app) openEnvironment(file string) {
	a.data.AddRecentEnvironment(file)
	a.data.Save()
}
