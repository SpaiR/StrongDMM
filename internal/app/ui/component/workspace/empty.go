package workspace

import (
	"fmt"
	"strings"
	"time"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/dm/dmenv"
	"github.com/SpaiR/strongdmm/pkg/imguiext"
)

type EmptyAction interface {
	DoOpenEnvironment()
	DoOpenEnvironmentByPath(path string)
	DoOpenMap()
	DoOpenMapByPath(path string)
	HasLoadedEnvironment() bool
	RecentEnvironments() []string
	RecentMapsByLoadedEnvironment() []string
	LoadedEnvironment() *dmenv.Dme
}

type Empty struct {
	base

	action EmptyAction
	name   string
}

func NewEmpty(action EmptyAction) Workspace {
	w := &Empty{
		action: action,
		name:   fmt.Sprint("New##workspace_empty_", time.Now().Nanosecond()),
	}
	w.Workspace = w
	return w
}

func (e *Empty) Name() string {
	return e.name
}

func (e *Empty) Process() {
	if !e.action.HasLoadedEnvironment() {
		if imgui.Button("Open Environment...") {
			e.action.DoOpenEnvironment()
		}
		imgui.Separator()
		for _, envPath := range e.action.RecentEnvironments() {
			if imgui.SmallButton(envPath) {
				e.action.DoOpenEnvironmentByPath(envPath)
			}
		}
	} else {
		imgui.Text(fmt.Sprint("Environment: ", e.action.LoadedEnvironment().RootFile))
		imgui.Separator()
		if imgui.Button("Open Map...") {
			e.action.DoOpenMap()
		}
		imgui.Separator()
		imgui.TextColored(imguiext.ColorGold, "Recent Maps:")
		for _, mapPath := range e.action.RecentMapsByLoadedEnvironment() {
			if imgui.SmallButton(sanitizeMapPath(e.action.LoadedEnvironment().RootDir, mapPath)) {
				e.action.DoOpenMapByPath(mapPath)
			}
		}
	}
}

func (*Empty) Border() bool {
	return true
}

func sanitizeMapPath(envRootDir, mapPath string) string {
	return strings.Replace(mapPath, envRootDir, "", 1)[1:]
}
