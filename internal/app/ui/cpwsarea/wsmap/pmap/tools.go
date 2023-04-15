package pmap

import (
	"sdmm/internal/app/ui/cpwsarea/wsmap/tools"
	"sdmm/internal/app/window"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
	"github.com/rs/zerolog/log"
)

func init() {
	window.RunRepeat(func() {
		processTempToolsMode()
	})
}

var (
	tmpToolIsInTemporalMode bool
	tmpToolLastSelectedName string
	tmpToolPrevSelectedName string
)

func processTempToolsMode() {
	if !tmpToolIsInTemporalMode {
		tmpToolLastSelectedName = tools.Selected().Name()
	}

	var inMode bool
	inMode = inMode || processTempToolMode(int(glfw.KeyS), -1, tools.TNPick)
	inMode = inMode || processTempToolMode(int(glfw.KeyD), -1, tools.TNDelete)
	inMode = inMode || processTempToolMode(int(glfw.KeyR), -1, tools.TNReplace)

	if tmpToolIsInTemporalMode && !inMode {
		log.Print("select before-tmp tool:", tmpToolLastSelectedName)
		tools.SetSelected(tmpToolLastSelectedName)
		tmpToolLastSelectedName = ""
		tmpToolIsInTemporalMode = false
	}
}

func processTempToolMode(key, altKey int, modeName string) bool {
	// Ignore presses when Dear ImGui inputs are in charge or actual shortcuts are invisible.
	{
		var p *PaneMap
		if activePane != nil {
			p = activePane
		} else if lastActivePane != nil {
			p = lastActivePane
		}
		if p != nil && !(p.canvasControl.Active() || p.shortcuts.Visible()) {
			return false
		}
	}

	isKeyPressed := imgui.IsKeyPressedV(key, false) || imgui.IsKeyPressedV(altKey, false)
	isKeyReleased := imgui.IsKeyReleased(key) || imgui.IsKeyReleased(altKey)
	isKeyDown := imgui.IsKeyDown(key) || imgui.IsKeyDown(altKey)
	isSelected := tools.IsSelected(modeName)

	if isKeyPressed && !isSelected {
		log.Print("selecting tmp tool:", modeName)
		tmpToolPrevSelectedName = tools.Selected().Name()
		tmpToolIsInTemporalMode = true
		tools.SetSelected(modeName)
	} else if isKeyReleased && len(tmpToolPrevSelectedName) != 0 {
		if isSelected {
			log.Print("selecting prev-tmp tool:", tmpToolPrevSelectedName)
			tools.SetSelected(tmpToolPrevSelectedName)
		}
		tmpToolPrevSelectedName = modeName
	}

	return isKeyDown
}

func selectAddTool() {
	tools.SetSelected(tools.TNAdd)
}

func selectFillTool() {
	tools.SetSelected(tools.TNFill)
}

func selectSelectTool() {
	tools.SetSelected(tools.TNGrab)
}
