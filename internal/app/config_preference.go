package app

import (
	"os/exec"
	"sdmm/internal/app/config"
	"sdmm/internal/app/prefs"
	"sdmm/internal/app/window"

	"github.com/rs/zerolog/log"
)

const (
	preferencesConfigName    = "preferences"
	preferencesConfigVersion = 3
)

type preferencesConfig struct {
	Version uint
	prefs.Prefs
}

func (preferencesConfig) Name() string {
	return preferencesConfigName
}

func (preferencesConfig) TryMigrate(cfg map[string]any) (result map[string]any, migrated bool) {
	result = cfg
	version := uint(result["Version"].(float64))

	if version == 1 {
		log.Print("migrating [preferences] config:", 2)

		result["Editor"] = result["Save"]
		delete(result, "Save")

		editorPrefs := result["Editor"].(map[string]any)
		editorPrefs["SaveFormat"] = editorPrefs["Format"]
		delete(editorPrefs, "Format")

		result["Version"] = 2
		migrated = true
	}
	if version == 2 {
		log.Print("migrating [preferences] config:", 3)

		editorPrefs := result["Editor"].(map[string]any)
		saveFormat := editorPrefs["SaveFormat"].(string)

		if saveFormat == "DM" {
			editorPrefs["SaveFormat"] = "DMM"
		}

		result["Version"] = 3
		migrated = true
	}

	return
}

func (a *app) loadPreferencesConfig() {
	cfg := &preferencesConfig{
		Version: preferencesConfigVersion,

		Prefs: prefs.Prefs{
			Interface: prefs.Interface{
				Scale: 100,
				Fps:   60,
			},
			Controls: prefs.Controls{
				QuickEditMapPane: true,
			},
			Editor: prefs.Editor{
				SaveFormat: prefs.SaveFormatInitial,
				CodeEditor: prefs.CodeEditorVSC,
				NudgeMode:  prefs.SaveNudgeModePixel,
			},
			Application: prefs.Application{
				CheckForUpdates: true,
				AutoUpdate:      true,
			},
		},
	}

	a.ConfigRegister(cfg)

	window.SetFps(cfg.Prefs.Interface.Fps)

	// Ensure code editor is possible otherwise default
	configFilePath := configFilePath(a.configDir, cfg.Name())
	switch cfg.Prefs.Editor.CodeEditor {
	case prefs.CodeEditorVSC:
		if _, err := exec.LookPath(prefs.CodeEditorVSCActual); err != nil {
			if _, err := exec.LookPath(prefs.CodeEditorDMActual); err == nil {
				// VSC invalid but DM valid
				cfg.Editor.CodeEditor = prefs.CodeEditorDM
			} else if _, err := exec.LookPath(prefs.CodeEditorNPPActual); err == nil {
				// VSC invalid but NPP valid
				cfg.Editor.CodeEditor = prefs.CodeEditorNPP
			} else {
				// All invalid just default
				cfg.Editor.CodeEditor = prefs.CodeEditorDefault
			}
			log.Print(prefs.CodeEditorVSCActual + " could not be found in PATH. Code Editor pref changed to " + cfg.Editor.CodeEditor)
			config.SaveV(configFilePath, cfg)
		}
	case prefs.CodeEditorDM:
		if _, err := exec.LookPath(prefs.CodeEditorDMActual); err != nil {
			if _, err := exec.LookPath(prefs.CodeEditorVSCActual); err == nil {
				// DM invalid but VSC valid
				cfg.Editor.CodeEditor = prefs.CodeEditorVSC
			} else if _, err := exec.LookPath(prefs.CodeEditorNPPActual); err == nil {
				// DM invalid but NPP valid
				cfg.Editor.CodeEditor = prefs.CodeEditorNPP
			} else {
				// All invalid just default
				cfg.Editor.CodeEditor = prefs.CodeEditorDefault
			}
			log.Print(prefs.CodeEditorDMActual + " could not be found in PATH. Code Editor pref changed to " + cfg.Editor.CodeEditor)
			config.SaveV(configFilePath, cfg)
		}
	case prefs.CodeEditorNPP:
		if _, err := exec.LookPath(prefs.CodeEditorNPPActual); err != nil {
			if _, err := exec.LookPath(prefs.CodeEditorVSCActual); err == nil {
				// NPP invalid but VSC valid
				cfg.Editor.CodeEditor = prefs.CodeEditorVSC
			} else if _, err := exec.LookPath(prefs.CodeEditorDMActual); err == nil {
				// NPP invalid but DM valid
				cfg.Editor.CodeEditor = prefs.CodeEditorDM
			} else {
				// All invalid just default
				cfg.Editor.CodeEditor = prefs.CodeEditorDefault
			}
			log.Print(prefs.CodeEditorNPPActual + " could not be found in PATH. Code Editor pref changed to " + cfg.Editor.CodeEditor)
			config.SaveV(configFilePath, cfg)
		}
	}
}

func (a *app) preferencesConfig() *preferencesConfig {
	if cfg, ok := a.ConfigFind(preferencesConfigName).(*preferencesConfig); ok {
		return cfg
	}
	log.Fatal().Msg("can't find project config")
	return nil
}
