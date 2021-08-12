package workspace

import (
	"fmt"
	"strings"
	"time"

	"github.com/SpaiR/imgui-go"
	"strongdmm/dm/dmenv"
)

type EmptyAction interface {
	AppDoOpenEnvironment()
	AppDoOpenEnvironmentByPath(path string)
	AppDoOpenMap()
	AppDoOpenMapByPath(path string)
	AppHasLoadedEnvironment() bool
	AppRecentEnvironments() []string
	AppRecentMapsByLoadedEnvironment() []string
	AppLoadedEnvironment() *dmenv.Dme
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
	if !e.action.AppHasLoadedEnvironment() {
		if imgui.Button("Open Environment...") {
			e.action.AppDoOpenEnvironment()
		}
		imgui.Separator()
		imgui.Text("Recent Environments:")
		for _, envPath := range e.action.AppRecentEnvironments() {
			if imgui.SmallButton(envPath) {
				e.action.AppDoOpenEnvironmentByPath(envPath)
			}
		}
	} else {
		imgui.Text(fmt.Sprint("Environment: ", e.action.AppLoadedEnvironment().RootFile))
		imgui.Separator()
		if imgui.Button("Open Map...") {
			e.action.AppDoOpenMap()
		}
		imgui.Separator()
		imgui.Text("Recent Maps:")
		for _, mapPath := range e.action.AppRecentMapsByLoadedEnvironment() {
			if imgui.SmallButton(sanitizeMapPath(e.action.AppLoadedEnvironment().RootDir, mapPath)) {
				e.action.AppDoOpenMapByPath(mapPath)
			}
		}
	}
}

func sanitizeMapPath(envRootDir, mapPath string) string {
	return strings.Replace(mapPath, envRootDir, "", 1)[1:]
}
