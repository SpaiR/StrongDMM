package app

import (
	"errors"
	"log"
	"os"

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

func (cfg *projectConfig) TryMigrate(_ map[string]any) (result map[string]any, migrated bool) {
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

func (cfg *projectConfig) ClearMaps() {
	cfg.MapsByProject = make(map[string][]string)
	log.Println("[app] cleared maps by project")
}

func (cfg *projectConfig) RemoveEnvironment(envPath string) {
	cfg.Projects = slice.StrRemove(cfg.Projects, envPath)
}

func (cfg *projectConfig) RemoveMap(mapPathToRemove string) {
	for projectPath, mapPaths := range cfg.MapsByProject {
		for _, mapPath := range mapPaths {
			if mapPath == mapPathToRemove {
				cfg.MapsByProject[projectPath] = slice.StrRemove(cfg.MapsByProject[projectPath], mapPath)
				log.Printf("[app] removed map [%s] by project: [%s]", mapPath, projectPath)
				return
			}
		}
	}
}

func (a *app) loadProjectConfig() {
	config := &projectConfig{
		Version:       projectConfigVersion,
		MapsByProject: make(map[string][]string),
	}

	a.ConfigRegister(config)

	// Cleanup projects paths
	var pathsToRemove []string
	for _, projectPath := range config.Projects {
		if _, err := os.Stat(projectPath); errors.Is(err, os.ErrNotExist) {
			pathsToRemove = append(pathsToRemove, projectPath)
		}
	}
	for _, path := range pathsToRemove {
		config.Projects = slice.StrRemove(config.Projects, path)
		log.Println("[app] config cleanup, removed project path:", path)
	}

	// Cleanup maps paths
	for projectPath, mapPaths := range config.MapsByProject {
		pathsToRemove = nil
		for _, mapPath := range mapPaths {
			if _, err := os.Stat(mapPath); errors.Is(err, os.ErrNotExist) {
				pathsToRemove = append(pathsToRemove, mapPath)
			}
		}
		for _, path := range pathsToRemove {
			config.MapsByProject[projectPath] = slice.StrRemove(config.MapsByProject[projectPath], path)
			log.Println("[app] config cleanup, removed map path:", path)
		}
	}
}

func (a *app) projectConfig() *projectConfig {
	if cfg, ok := a.ConfigFind(projectConfigName).(*projectConfig); ok {
		return cfg
	}
	log.Fatal("[app] can't find project config")
	return nil
}
