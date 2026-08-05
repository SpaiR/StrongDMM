// Package extensionapi defines the RPC contract between StrongDMM and Go extensions.
package extensionapi

import (
	"net/rpc"

	"github.com/hashicorp/go-plugin"
)

const (
	ProtocolVersion = 1
	PluginName      = "strongdmm.extension"
	StageMap        = "core.map"
	StageSelection  = "core.selection"
	StageOverlays   = "core.overlays"

	CapabilityRender      = "render"
	CapabilityContextMenu = "context-menu"
	CapabilityAppearance  = "appearance"
)

type Color struct{ R, G, B float32 }
type AtomDefinition struct {
	ID           uint32
	Path         string
	Vars         map[string]string
	ResolvedVars map[string]string
}

// AtomInstance identifies one map atom. DefinitionID describes its resolved
// prefab while ID remains stable for the lifetime of the editor instance.
type AtomInstance struct {
	ID           uint64
	DefinitionID uint32
}
type Tile struct {
	X, Y, Z int
	Atoms   []AtomInstance
}
type Map struct {
	Width, Height, Levels int
	Definitions           []AtomDefinition
	Tiles                 []Tile
}
type Vertex struct{ X, Y, R, G, B, A float32 }
type BilinearQuad struct {
	X1, Y1, X2, Y2                             float32
	SouthWest, SouthEast, NorthWest, NorthEast Color
}
type Mesh struct {
	Vertices []Vertex
	Indices  []uint32
	Quads    []BilinearQuad
}
type RenderCommand struct {
	ID    string
	Kind  string
	Level int // 0 renders on every level; otherwise only this map Z level.
	Mesh  Mesh
}
type RenderPass struct {
	ID            string
	After, Before []string
	Blend         string
	ColorFloor    float32
	Commands      []RenderCommand
}
type RenderPatch struct {
	PassID string
	Upsert []RenderCommand
	Remove []string
}
type RenderUpdate struct {
	Replace      []RenderPass
	UpsertPasses []RenderPass
	Patches      []RenderPatch
	RemovePasses []string
}

// Appearance is a partial visual appearance. Nil pointers mean that the field
// is left unchanged, allowing several extensions to compose their patches.
type Appearance struct {
	Icon, IconState *string
	Dir             *int
	Color           *string
	Alpha           *float32
	PixelX, PixelY  *int
	PixelW, PixelZ  *int
	Visible         *bool
}
type AppearancePatch struct {
	AtomID     uint64
	Appearance Appearance
	Underlays  []Appearance
	Overlays   []Appearance
}
type AppearanceUpdate struct {
	Reset  bool
	Upsert []AppearancePatch
	Remove []uint64
}
type MapUpdate struct {
	Width, Height, Levels int
	Definitions           []AtomDefinition
	Tiles                 []Tile
}
type ContextTurf struct {
	X, Y, Z int
	Atoms   []AtomDefinition
}
type ContextMenuRequest struct{ Turf ContextTurf }
type ContextMenuAction struct {
	ID, Label string
	Enabled   bool
}
type ContextMenuResponse struct{ Actions []ContextMenuAction }
type ActionRequest struct {
	ID   string
	Turf ContextTurf
}

// Request is a versioned extension operation. Extensions ignore operations
// outside the capabilities they declare in their package manifest.
type Request struct {
	Version     int
	Type        string
	MapID       string
	Revision    uint64
	Settings    map[string]bool
	Values      map[string]float64
	Map         *Map
	Update      *MapUpdate
	Render      *RenderUpdate
	Appearance  *AppearanceUpdate
	Context     *ContextMenuRequest
	Action      *ActionRequest
	ContextMenu *ContextMenuResponse
	Reason      string
}

// Response carries optional output for a request. Render and context-menu
// operations use their corresponding fields; other operations may return ack.
type Response = Request

// Extension handles versioned requests for its declared capabilities.
type Extension interface {
	Handle(Request) (Response, error)
}

// HandshakeConfig prevents StrongDMM from connecting to an unrelated process.
var HandshakeConfig = plugin.HandshakeConfig{
	ProtocolVersion:  ProtocolVersion,
	MagicCookieKey:   "STRONGDMM_EXTENSION",
	MagicCookieValue: "strongdmm-extension-v1",
}

// Plugin adapts an Extension to HashiCorp go-plugin's net/rpc transport.
type Plugin struct {
	plugin.NetRPCUnsupportedPlugin
	Impl Extension
}

func (p *Plugin) Server(*plugin.MuxBroker) (interface{}, error) {
	return &rpcServer{Impl: p.Impl}, nil
}

func (p *Plugin) Client(_ *plugin.MuxBroker, client *rpc.Client) (interface{}, error) {
	return &rpcClient{client: client}, nil
}

type rpcClient struct{ client *rpc.Client }

func (c *rpcClient) Handle(request Request) (Response, error) {
	var response Response
	err := c.client.Call("Plugin.Handle", request, &response)
	return response, err
}

type rpcServer struct{ Impl Extension }

func (s *rpcServer) Handle(request Request, response *Response) error {
	result, err := s.Impl.Handle(request)
	*response = result
	return err
}
