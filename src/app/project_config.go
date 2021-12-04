package app

import (
	"log"

	"sdmm/util/slice"
)

const (
	projectConfigName    = "project"
	projectConfigVersion = 1
)

type projectConfig struct {
	Version uint

	Projects      []string
	MapsByProject map[string][]string
}

func (projectConfig) Name() string {
	return projectConfigName
}

func (cfg *projectConfig) TryMigrate(_ map[string]interface{}) (result map[string]interface{}, migrated bool) {
	// do nothing. yet...
	return nil, migrated
}

func (cfg *projectConfig) AddProject(projectPath string) {
	cfg.Projects = slice.StrPushUnique(cfg.Projects, projectPath)
	log.Println("[app] added project:", projectPath)
}

func (cfg *projectConfig) ClearProjects() {
	cfg.Projects = nil
	log.Println("[app] cleared projects")
}

func (cfg *projectConfig) AddMapByProject(projectPath string, mapPath string) {
	maps := cfg.MapsByProject[projectPath]
	maps = slice.StrPushUnique(maps, mapPath)
	cfg.MapsByProject[projectPath] = maps
	log.Printf("[app] added map by project [%s]: %s", projectPath, mapPath)
}

func (cfg *projectConfig) ClearMapsByProject(projectPath string) {
	cfg.MapsByProject[projectPath] = nil
	log.Printf("[app] cleared maps by project [%s]", projectPath)
}

func (a *app) loadProjectConfig() {
	a.ConfigRegister(&projectConfig{
		Version:       projectConfigVersion,
		MapsByProject: make(map[string][]string),
	})
}

func (a *app) projectConfig() *projectConfig {
	if cfg, ok := a.ConfigFind(projectConfigName).(*projectConfig); ok {
		return cfg
	}
	log.Fatal("[app] can't find project config")
	return nil
}
