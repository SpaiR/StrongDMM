package workspace

import (
	"fmt"
	"time"
)

type Workspace struct {
	id string

	triggerFocus bool

	content content
}

func (ws *Workspace) TriggerFocus() bool {
	return ws.triggerFocus
}

func (ws *Workspace) SetTriggerFocus(triggerFocus bool) {
	ws.triggerFocus = triggerFocus
}

//goland:noinspection GoExportedFuncWithUnexportedType
func (ws *Workspace) Content() content {
	return ws.content
}

func (ws *Workspace) SetContent(cnt content) {
	if ws.content != nil {
		ws.content.Dispose()
	}
	ws.content = cnt
	cnt.setRoot(ws)
}

func New(cnt content) *Workspace {
	ws := &Workspace{content: cnt}
	cnt.setRoot(ws)
	return ws
}

func (ws *Workspace) String() string {
	return ws.Id()
}

func (ws *Workspace) Name() string {
	return ws.content.Name()
}

func (ws *Workspace) Title() string {
	return ws.content.Title()
}

var workspaceCount uint64

func (ws *Workspace) Id() string {
	if ws.id == "" {
		ws.id = fmt.Sprint("workspace_", time.Now().Nanosecond(), "_", workspaceCount)
		workspaceCount++
	}
	return ws.id
}

func (ws *Workspace) CommandStackId() string {
	return ws.content.CommandStackId()
}

func (ws *Workspace) OnFocusChange(focused bool) {
	ws.content.OnFocusChange(focused)
}

func (ws *Workspace) Initialize() {
	ws.content.Initialize()
}

func (ws *Workspace) PreProcess() {
	ws.content.PreProcess()
}

func (ws *Workspace) Process() {
	ws.content.Process()
}

func (ws *Workspace) PostProcess() {
	ws.content.PostProcess()
}

func (ws *Workspace) Dispose() {
	ws.content.Dispose()
}

func (ws *Workspace) Focused() bool {
	return ws.content.Focused()
}

func (ws *Workspace) Closed() bool {
	return ws.content.Closed()
}

func (ws *Workspace) Save() bool {
	return ws.Content().Save()
}
