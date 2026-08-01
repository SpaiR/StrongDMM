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
)

type Color struct{ R, G, B float32 }
type AtomDefinition struct {
	ID   uint32
	Path string
	Vars map[string]string
}
type Tile struct {
	X, Y, Z int
	Atoms   []uint32
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
	ID   string
	Kind string
	Mesh Mesh
}
type RenderPass struct {
	ID            string
	After, Before []string
	Blend         string
	Commands      []RenderCommand
}
type RenderPatch struct {
	PassID string
	Upsert []RenderCommand
	Remove []string
}
type RenderUpdate struct {
	Replace      []RenderPass
	Patches      []RenderPatch
	RemovePasses []string
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
	Map         *Map
	Update      *MapUpdate
	Render      *RenderUpdate
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
