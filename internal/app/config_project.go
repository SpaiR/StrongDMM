package app

import (
	"errors"
	"log"
	"os"

	"sdmm/internal/util/slice"
)

const (
	projectConfigName    = "project"
	projectConfigVersion = 2
)

type projectConfig struct {
	Version uint

	Projects []string
	Maps     []string
}

func (projectConfig) Name() string {
	return projectConfigName
}

func (projectConfig) TryMigrate(cfg map[string]any) (result map[string]any, migrated bool) {
	result = cfg

	if uint(result["Version"].(float64)) == 1 {
		log.Println("[app] migrating [project] config:", 2)

		mapsByProject := result["MapsByProject"].(map[string]any)
		var maps []string
		for projectPath := range mapsByProject {
			for _, mapPath := range mapsByProject[projectPath].([]interface{}) {
				maps = append(maps, mapPath.(string))
			}
		}
		result["Maps"] = maps
		delete(result, "MapsByProject")

		result["Version"] = 2
		migrated = true
	}

	return result, migrated
}

func (cfg *projectConfig) AddProject(projectPath string) {
	cfg.Projects = slice.StrPushUnique(cfg.Projects, projectPath)
	log.Println("[app] added project:", projectPath)
}

func (cfg *projectConfig) ClearProjects() {
	cfg.Projects = nil
	log.Println("[app] cleared projects")
}

func (cfg *projectConfig) AddMap(mapPath string) {
	cfg.Maps = slice.StrPushUnique(cfg.Maps, mapPath)
	log.Println("[app] added map:", mapPath)
}

func (cfg *projectConfig) ClearMaps() {
	cfg.Maps = nil
	log.Println("[app] cleared maps")
}

func (cfg *projectConfig) RemoveEnvironment(envPath string) {
	cfg.Projects = slice.StrRemove(cfg.Projects, envPath)
}

func (cfg *projectConfig) RemoveMap(mapPath string) {
	cfg.Maps = slice.StrRemove(cfg.Maps, mapPath)
	log.Println("[app] removed map:", mapPath)
}

func (a *app) loadProjectConfig() {
	config := &projectConfig{
		Version: projectConfigVersion,
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
	pathsToRemove = nil
	for _, mapPath := range config.Maps {
		if _, err := os.Stat(mapPath); errors.Is(err, os.ErrNotExist) {
			pathsToRemove = append(pathsToRemove, mapPath)
		}
		for _, path := range pathsToRemove {
			config.Maps = slice.StrRemove(config.Maps, path)
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
