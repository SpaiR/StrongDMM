package render

import (
	"sort"

	api "github.com/SpaiR/StrongDMM-extension-api"
	"sdmm/internal/app/render/brush"

	"github.com/rs/zerolog/log"
)

const (
	coreMap       = api.StageMap
	coreSelection = api.StageSelection
	coreOverlays  = api.StageOverlays
)

type graphNode struct {
	id   string
	pass *api.RenderPass
}

// SetExtensionPasses compiles extension pass declarations into the same graph
// as StrongDMM's core render stages.
func (r *Render) SetExtensionPasses(passes []api.RenderPass) {
	r.renderGraph = compileRenderGraph(passes)
}

func compileRenderGraph(passes []api.RenderPass) []graphNode {
	nodes := []graphNode{{id: coreMap}, {id: coreSelection}, {id: coreOverlays}}
	byID := map[string]int{coreMap: 0, coreSelection: 1, coreOverlays: 2}
	passes = append([]api.RenderPass(nil), passes...)
	sort.SliceStable(passes, func(i, j int) bool { return passes[i].ID < passes[j].ID })
	for i := range passes {
		if passes[i].ID == "" {
			log.Warn().Msg("ignoring extension render pass without an ID")
			continue
		}
		if _, exists := byID[passes[i].ID]; exists {
			log.Warn().Str("pass", passes[i].ID).Msg("ignoring duplicate extension render pass")
			continue
		}
		byID[passes[i].ID] = len(nodes)
		nodes = append(nodes, graphNode{id: passes[i].ID, pass: &passes[i]})
	}
	deps := make([]int, len(nodes))
	forward := make([][]int, len(nodes))
	addEdge := func(from, to int) {
		for _, next := range forward[from] {
			if next == to {
				return
			}
		}
		forward[from] = append(forward[from], to)
		deps[to]++
	}
	addEdge(byID[coreMap], byID[coreSelection])
	addEdge(byID[coreSelection], byID[coreOverlays])
	for i, node := range nodes {
		if node.pass == nil {
			continue
		}
		pass := node.pass
		validConstraint := false
		for _, id := range pass.After {
			if before, ok := byID[id]; ok {
				addEdge(before, i)
				validConstraint = true
			} else {
				log.Warn().Str("pass", pass.ID).Str("after", id).Msg("unknown extension render pass dependency")
			}
		}
		for _, id := range pass.Before {
			if after, ok := byID[id]; ok {
				addEdge(i, after)
				validConstraint = true
			} else {
				log.Warn().Str("pass", pass.ID).Str("before", id).Msg("unknown extension render pass dependency")
			}
		}
		if !validConstraint {
			addEdge(byID[coreMap], i)
			addEdge(i, byID[coreSelection])
		}
	}
	ordered := make([]graphNode, 0, len(nodes))
	for len(ordered) < len(nodes) {
		found := -1
		for i := range nodes {
			if deps[i] == 0 {
				found = i
				break
			}
		}
		if found < 0 {
			log.Warn().Msg("extension render pass graph contains a cycle; using fallback order")
			return fallbackRenderGraph(nodes)
		}
		deps[found] = -1
		ordered = append(ordered, nodes[found])
		for _, next := range forward[found] {
			deps[next]--
		}
	}
	return ordered
}

func fallbackRenderGraph(nodes []graphNode) []graphNode {
	ordered := []graphNode{nodes[0]}
	for _, node := range nodes[3:] {
		ordered = append(ordered, node)
	}
	return append(ordered, nodes[1], nodes[2])
}

func (r *Render) drawExtensionPass(pass api.RenderPass, width, height float32) {
	switch pass.Blend {
	case "multiply":
		r.setMultiplyBlend()
	case "add":
		r.setAddBlend()
	default:
		r.setNormalBlend()
	}
	for _, command := range pass.Commands {
		switch command.Kind {
		case "mesh":
			vertices := make([]brush.Vertex, 0, len(command.Mesh.Vertices))
			for _, v := range command.Mesh.Vertices {
				vertices = append(vertices, brush.Vertex{X: v.X, Y: v.Y, R: v.R, G: v.G, B: v.B, A: v.A})
			}
			brush.ColoredTriangles(vertices, command.Mesh.Indices)
		case "bilinear":
			brush.Draw(width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)
			quads := make([]brush.BilinearQuad, 0, len(command.Mesh.Quads))
			for _, quad := range command.Mesh.Quads {
				quads = append(quads, brush.BilinearQuad{X1: quad.X1, Y1: quad.Y1, X2: quad.X2, Y2: quad.Y2, SouthWest: brush.Color(quad.SouthWest), SouthEast: brush.Color(quad.SouthEast), NorthWest: brush.Color(quad.NorthWest), NorthEast: brush.Color(quad.NorthEast)})
			}
			brush.BilinearQuads(quads, width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)
		}
	}
	brush.Draw(width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)
	r.setNormalBlend()
}
