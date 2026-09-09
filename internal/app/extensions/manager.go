package extensions

import (
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"time"

	api "github.com/SpaiR/StrongDMM-extension-api"
	"github.com/hashicorp/go-plugin"
	"sdmm/internal/app/render"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmvars"
	"sdmm/internal/util"

	"github.com/rs/zerolog/log"
)

type State struct{ Extensions map[string]*ExtensionState }
type ExtensionState struct {
	Disabled bool
	Preview  bool
	Settings map[string]bool
	Values   map[string]int
}
type MenuAction struct {
	ID, Menu, Label   string
	Enabled, Selected bool
	Kind              string
	Value, Min, Max   int
}
type ContextMenuAction struct {
	ID, Label string
	Enabled   bool
}

type Manager struct {
	state     *State
	clients   []*client
	documents map[*dmmap.Dmm]*document
}

type document struct {
	id             string
	revision       uint64
	opened         bool
	definitions    map[string]uint32
	atoms          map[uint32]api.AtomDefinition
	nextDefinition uint32
	renderers      map[*render.Render]struct{}
	outputs        map[*client]map[string]api.RenderPass
	appearances    map[*client]map[uint64]api.AppearancePatch
	applied        map[*client]uint64
	newDefinitions []api.AtomDefinition
}

func New(state *State, extensionsDir string) *Manager {
	if state.Extensions == nil {
		state.Extensions = make(map[string]*ExtensionState)
	}
	m := &Manager{state: state, clients: discover(extensionsDir), documents: make(map[*dmmap.Dmm]*document)}
	for _, client := range m.clients {
		m.stateFor(client)
		client.startWorker()
	}
	return m
}

func (m *Manager) stateFor(client *client) *ExtensionState {
	if state := m.state.Extensions[client.manifest.ID]; state != nil {
		return state
	}
	state := &ExtensionState{Preview: true}
	m.state.Extensions[client.manifest.ID] = state
	return state
}
func (m *Manager) enabled(client *client) bool { return !m.stateFor(client).Disabled }
func (m *Manager) preview(client *client) bool { return m.stateFor(client).Preview }
func (m *Manager) settings(client *client) map[string]bool {
	settings := make(map[string]bool)
	for id, value := range m.stateFor(client).Settings {
		settings[id] = value
	}
	return settings
}
func (m *Manager) values(client *client) map[string]float64 {
	values := make(map[string]float64)
	for id, value := range m.stateFor(client).Values {
		values[id] = float64(value)
	}
	return values
}

func (m *Manager) MenuActions() []MenuAction {
	actions := make([]MenuAction, 0)
	for _, client := range m.clients {
		for _, action := range client.manifest.Actions {
			selected, enabled := false, true
			switch action.Kind {
			case "toggle-enabled":
				selected = m.enabled(client)
			case "toggle-preview":
				selected, enabled = m.preview(client), m.enabled(client)
			case "toggle":
				selected, enabled = m.stateFor(client).Settings[action.ID], m.enabled(client)
			case "slider":
				enabled = m.enabled(client)
			default:
				continue
			}
			value := m.stateFor(client).Values[action.ID]
			if value == 0 && action.Default != 0 {
				value = action.Default
			}
			actions = append(actions, MenuAction{ID: client.manifest.ID + ":" + action.ID, Menu: action.Menu, Label: action.Label, Enabled: enabled, Selected: selected, Kind: action.Kind, Value: value, Min: action.Min, Max: action.Max})
		}
	}
	return actions
}
func (m *Manager) SetMenuValue(id string, value int) {
	for _, client := range m.clients {
		for _, action := range client.manifest.Actions {
			if action.Kind != "slider" || id != client.manifest.ID+":"+action.ID {
				continue
			}
			if value < action.Min {
				value = action.Min
			}
			if value > action.Max {
				value = action.Max
			}
			state := m.stateFor(client)
			if state.Values == nil {
				state.Values = make(map[string]int)
			}
			state.Values[action.ID] = value
			if action.RenderPass != "" && action.RenderProperty != "" {
				for _, doc := range m.documents {
					m.applyRenderSetting(client, doc)
					m.render(doc)
				}
				return
			}
			m.refreshClient(client)
			return
		}
	}
}

func (m *Manager) ExecuteMenuAction(id string) {
	for _, client := range m.clients {
		for _, action := range client.manifest.Actions {
			if id != client.manifest.ID+":"+action.ID {
				continue
			}
			state := m.stateFor(client)
			switch action.Kind {
			case "toggle-enabled":
				state.Disabled = !state.Disabled
			case "toggle-preview":
				state.Preview = !state.Preview
				for _, doc := range m.documents {
					m.render(doc)
				}
				return
			case "toggle":
				if state.Settings == nil {
					state.Settings = make(map[string]bool)
				}
				state.Settings[action.ID] = !state.Settings[action.ID]
			}
			m.refreshClient(client)
			return
		}
	}
}

// ContextMenuActions returns dynamic actions contributed for one turf.
func (m *Manager) ContextMenuActions(dmm *dmmap.Dmm, point util.Point) []ContextMenuAction {
	doc := m.document(dmm)
	turf := m.contextTurf(dmm, dmm.GetTile(point))
	actions := make([]ContextMenuAction, 0)
	for _, client := range m.clients {
		if !m.enabled(client) || !client.hasCapability(api.CapabilityContextMenu) {
			continue
		}
		response, err := client.extension.Handle(api.Request{Version: api.ProtocolVersion, Type: "context.menu", MapID: doc.id, Revision: doc.revision, Context: &api.ContextMenuRequest{Turf: turf}})
		if err != nil || response.ContextMenu == nil {
			if err != nil {
				log.Warn().Err(err).Str("extension", client.manifest.ID).Msg("extension context menu request failed")
			}
			continue
		}
		for _, action := range response.ContextMenu.Actions {
			if action.ID == "" || action.Label == "" {
				continue
			}
			actions = append(actions, ContextMenuAction{ID: client.manifest.ID + ":" + action.ID, Label: action.Label, Enabled: action.Enabled})
		}
	}
	return actions
}

// ExecuteContextMenuAction notifies the extension that supplied an action.
func (m *Manager) ExecuteContextMenuAction(dmm *dmmap.Dmm, point util.Point, id string) {
	doc := m.document(dmm)
	for _, client := range m.clients {
		prefix := client.manifest.ID + ":"
		if !m.enabled(client) || !client.hasCapability(api.CapabilityContextMenu) || len(id) <= len(prefix) || id[:len(prefix)] != prefix {
			continue
		}
		response, err := client.extension.Handle(api.Request{Version: api.ProtocolVersion, Type: "context.action", MapID: doc.id, Revision: doc.revision, Action: &api.ActionRequest{ID: id[len(prefix):], Turf: m.contextTurf(dmm, dmm.GetTile(point))}})
		if err != nil {
			log.Warn().Err(err).Str("extension", client.manifest.ID).Msg("extension context action failed")
			return
		}
		m.apply(client, workerResult{message: api.Request{Version: response.Version, Type: response.Type, MapID: response.MapID, Revision: response.Revision, Render: response.Render, Appearance: response.Appearance}})
		return
	}
}

func (m *Manager) ConfigureRender(renderer *render.Render, dmm *dmmap.Dmm) {
	doc := m.document(dmm)
	doc.renderers[renderer] = struct{}{}
	renderer.SetExtensionPasses(m.extensionPasses(doc))
	renderer.SetAppearancePatches(m.extensionAppearances(doc))
	if !doc.opened {
		m.queueOpen(doc, dmm)
	}
}

func (m *Manager) RefreshRender(renderer *render.Render, dmm *dmmap.Dmm) {
	m.ConfigureRender(renderer, dmm)
	m.queueOpen(m.document(dmm), dmm)
}

// ReleaseRender disconnects a canvas from its map's extension output. The
// extension receives map.close only after the last canvas using the map closes.
func (m *Manager) ReleaseRender(renderer *render.Render, dmm *dmmap.Dmm) {
	doc := m.documents[dmm]
	if doc == nil {
		return
	}
	delete(doc.renderers, renderer)
	if len(doc.renderers) != 0 {
		return
	}
	m.closeDocument(dmm, doc)
}

// ReplaceRender swaps a canvas renderer without interrupting the extension's
// map session, for example while recreating a canvas after a resize.
func (m *Manager) ReplaceRender(previous, next *render.Render, dmm *dmmap.Dmm) {
	doc := m.documents[dmm]
	if doc == nil {
		m.ConfigureRender(next, dmm)
		return
	}
	delete(doc.renderers, previous)
	doc.renderers[next] = struct{}{}
	next.SetExtensionPasses(m.extensionPasses(doc))
	next.SetAppearancePatches(m.extensionAppearances(doc))
}

func (m *Manager) NotifyChanged(dmm *dmmap.Dmm, points []util.Point) {
	doc := m.document(dmm)
	doc.revision++
	update := m.update(doc, dmm, points)
	for _, client := range m.clients {
		if m.enabled(client) && client.usesMap() {
			client.queue(work{message: api.Request{Version: api.ProtocolVersion, Type: "map.update", MapID: doc.id, Revision: doc.revision, Values: m.values(client), Update: &update}})
		}
	}
}

func (m *Manager) NotifyResize(dmm *dmmap.Dmm) {
	doc := m.document(dmm)
	doc.revision++
	m.queueOpen(doc, dmm)
}

func (m *Manager) Process() {
	for _, client := range m.clients {
		for {
			select {
			case result := <-client.results:
				m.apply(client, result)
			default:
				goto next
			}
		}
	next:
	}
}

func (m *Manager) closeDocument(dmm *dmmap.Dmm, doc *document) {
	for _, client := range m.clients {
		client.queue(work{message: api.Request{Version: api.ProtocolVersion, Type: "map.close", MapID: doc.id, Revision: doc.revision}})
	}
	delete(m.documents, dmm)
}
func (m *Manager) Dispose() {
	for _, client := range m.clients {
		client.close()
	}
}

func (m *Manager) document(dmm *dmmap.Dmm) *document {
	if doc := m.documents[dmm]; doc != nil {
		return doc
	}
	doc := &document{id: fmt.Sprintf("map-%p", dmm), definitions: make(map[string]uint32), atoms: make(map[uint32]api.AtomDefinition), nextDefinition: 1, renderers: make(map[*render.Render]struct{}), outputs: make(map[*client]map[string]api.RenderPass), appearances: make(map[*client]map[uint64]api.AppearancePatch), applied: make(map[*client]uint64)}
	m.documents[dmm] = doc
	return doc
}

func (m *Manager) queueOpen(doc *document, dmm *dmmap.Dmm) {
	if doc.revision == 0 {
		doc.revision = 1
	}
	doc.opened = true
	mapData := m.mapData(doc, dmm, nil)
	for _, client := range m.clients {
		if m.enabled(client) && client.usesMap() {
			client.queue(work{immediate: true, message: api.Request{Version: api.ProtocolVersion, Type: "map.open", MapID: doc.id, Revision: doc.revision, Settings: m.settings(client), Values: m.values(client), Map: &mapData}})
		}
	}
}

func (m *Manager) refreshClient(client *client) {
	for dmm, doc := range m.documents {
		if !m.enabled(client) || !client.usesMap() {
			delete(doc.outputs, client)
			delete(doc.appearances, client)
			delete(doc.applied, client)
			m.render(doc)
			continue
		}
		m.render(doc)
		if _, tracked := doc.applied[client]; tracked {
			client.queue(work{immediate: true, message: api.Request{Version: api.ProtocolVersion, Type: "settings.update", MapID: doc.id, Revision: doc.revision, Settings: m.settings(client), Values: m.values(client)}})
			continue
		}
		mapData := m.mapData(doc, dmm, nil)
		client.queue(work{immediate: true, message: api.Request{Version: api.ProtocolVersion, Type: "map.open", MapID: doc.id, Revision: doc.revision, Settings: m.settings(client), Values: m.values(client), Map: &mapData}})
	}
}

func (m *Manager) apply(client *client, result workerResult) {
	var doc *document
	for _, candidate := range m.documents {
		if candidate.id == result.message.MapID {
			doc = candidate
			break
		}
	}
	if doc == nil {
		return
	}
	if result.err != nil {
		log.Error().Err(result.err).Str("extension", client.manifest.ID).Msg("extension request failed")
		return
	}
	message := result.message
	if message.Type == "resync.request" {
		for dmm, candidate := range m.documents {
			if candidate == doc {
				m.queueOpen(doc, dmm)
				return
			}
		}
	}
	if (message.Type != "render.update" && message.Type != "appearance.update") || message.Revision < doc.applied[client] {
		return
	}
	if message.Appearance != nil {
		patches := doc.appearances[client]
		if message.Appearance.Reset || patches == nil {
			patches = make(map[uint64]api.AppearancePatch)
			doc.appearances[client] = patches
		}
		for _, id := range message.Appearance.Remove {
			delete(patches, id)
		}
		for _, patch := range message.Appearance.Upsert {
			if patch.AtomID != 0 {
				patches[patch.AtomID] = patch
			}
		}
	}
	if message.Render == nil {
		doc.applied[client] = message.Revision
		m.render(doc)
		return
	}
	passes := doc.outputs[client]
	if passes == nil {
		passes = make(map[string]api.RenderPass)
		doc.outputs[client] = passes
	}
	if message.Render.Replace != nil {
		passes = make(map[string]api.RenderPass)
		for _, pass := range message.Render.Replace {
			passes[pass.ID] = pass
		}
		doc.outputs[client] = passes
	}
	for _, pass := range message.Render.UpsertPasses {
		passes[pass.ID] = pass
	}
	for _, passID := range message.Render.RemovePasses {
		delete(passes, passID)
	}
	for _, patch := range message.Render.Patches {
		pass, ok := passes[patch.PassID]
		if !ok {
			continue
		}
		commands := make(map[string]api.RenderCommand)
		for _, command := range pass.Commands {
			commands[command.ID] = command
		}
		for _, id := range patch.Remove {
			delete(commands, id)
		}
		for _, command := range patch.Upsert {
			commands[command.ID] = command
		}
		pass.Commands = pass.Commands[:0]
		for _, command := range commands {
			pass.Commands = append(pass.Commands, command)
		}
		sort.Slice(pass.Commands, func(i, j int) bool { return pass.Commands[i].ID < pass.Commands[j].ID })
		passes[pass.ID] = pass
	}
	m.applyRenderSetting(client, doc)
	doc.applied[client] = message.Revision
	m.render(doc)
}
func (m *Manager) applyRenderSetting(client *client, doc *document) {
	passes := doc.outputs[client]
	for _, action := range client.manifest.Actions {
		if action.Kind != "slider" || action.RenderPass == "" {
			continue
		}
		pass, exists := passes[action.RenderPass]
		if !exists {
			continue
		}
		value := m.stateFor(client).Values[action.ID]
		if value == 0 && action.Default != 0 {
			value = action.Default
		}
		switch action.RenderProperty {
		case "color_floor":
			pass.ColorFloor = float32(value) / 100
		}
		passes[pass.ID] = pass
	}
}

func (m *Manager) render(doc *document) {
	passes := m.extensionPasses(doc)
	appearances := m.extensionAppearances(doc)
	for renderer := range doc.renderers {
		renderer.SetExtensionPasses(passes)
		renderer.SetAppearancePatches(appearances)
	}
}

func (m *Manager) extensionAppearances(doc *document) map[uint64]api.AppearancePatch {
	result := make(map[uint64]api.AppearancePatch)
	for _, client := range m.clients {
		if !m.preview(client) {
			continue
		}
		for id, patch := range doc.appearances[client] {
			previous := result[id]
			previous.AtomID = id
			previous.Appearance = mergeAppearance(previous.Appearance, patch.Appearance)
			previous.Underlays = append(previous.Underlays, patch.Underlays...)
			previous.Overlays = append(previous.Overlays, patch.Overlays...)
			result[id] = previous
		}
	}
	return result
}

func mergeAppearance(base, patch api.Appearance) api.Appearance {
	if patch.Icon != nil {
		base.Icon = patch.Icon
	}
	if patch.IconState != nil {
		base.IconState = patch.IconState
	}
	if patch.Dir != nil {
		base.Dir = patch.Dir
	}
	if patch.Color != nil {
		base.Color = patch.Color
	}
	if patch.Alpha != nil {
		base.Alpha = patch.Alpha
	}
	if patch.PixelX != nil {
		base.PixelX = patch.PixelX
	}
	if patch.PixelY != nil {
		base.PixelY = patch.PixelY
	}
	if patch.PixelW != nil {
		base.PixelW = patch.PixelW
	}
	if patch.PixelZ != nil {
		base.PixelZ = patch.PixelZ
	}
	if patch.Visible != nil {
		base.Visible = patch.Visible
	}
	return base
}

// extensionPasses produces a stable global pass list. Client discovery is
// already sorted by extension ID; sorting each client's pass IDs keeps output
// stable even though they are stored in a map.
func (m *Manager) extensionPasses(doc *document) []api.RenderPass {
	passes := make([]api.RenderPass, 0)
	seen := make(map[string]struct{})
	for _, client := range m.clients {
		if !m.preview(client) {
			continue
		}
		clientPasses := doc.outputs[client]
		ids := make([]string, 0, len(clientPasses))
		for id := range clientPasses {
			ids = append(ids, id)
		}
		sort.Strings(ids)
		for _, id := range ids {
			pass := clientPasses[id]
			if _, duplicate := seen[pass.ID]; duplicate {
				log.Warn().Str("extension", client.manifest.ID).Str("pass", pass.ID).Msg("ignoring duplicate extension render pass")
				continue
			}
			seen[pass.ID] = struct{}{}
			passes = append(passes, pass)
		}
	}
	return passes
}

func (m *Manager) mapData(doc *document, dmm *dmmap.Dmm, points []util.Point) api.Map {
	mapData := api.Map{Width: dmm.MaxX, Height: dmm.MaxY, Levels: dmm.MaxZ}
	full := points == nil
	doc.newDefinitions = nil
	if points == nil {
		points = make([]util.Point, 0, len(dmm.Tiles))
		for _, tile := range dmm.Tiles {
			points = append(points, tile.Coord)
		}
	}
	for _, point := range points {
		mapData.Tiles = append(mapData.Tiles, m.tile(doc, dmm, dmm.GetTile(point)))
	}
	if full {
		for _, atom := range doc.atoms {
			mapData.Definitions = append(mapData.Definitions, atom)
		}
		sort.Slice(mapData.Definitions, func(i, j int) bool { return mapData.Definitions[i].ID < mapData.Definitions[j].ID })
	} else {
		mapData.Definitions = doc.newDefinitions
	}
	return mapData
}
func (m *Manager) update(doc *document, dmm *dmmap.Dmm, points []util.Point) api.MapUpdate {
	mapData := m.mapData(doc, dmm, points)
	return api.MapUpdate{Width: mapData.Width, Height: mapData.Height, Levels: mapData.Levels, Definitions: mapData.Definitions, Tiles: mapData.Tiles}
}
func (m *Manager) tile(doc *document, dmm *dmmap.Dmm, tile *dmmap.Tile) api.Tile {
	out := api.Tile{X: tile.Coord.X, Y: tile.Coord.Y, Z: tile.Coord.Z}
	for _, instance := range tile.Instances() {
		vars := make(map[string]string)
		resolvedVars := make(map[string]string)
		prefabVars := instance.Prefab().Vars()
		keys := resolvedVariableNames(prefabVars)
		sort.Strings(keys)
		for _, key := range keys {
			if value, ok := prefabVars.Value(key); ok {
				vars[key] = value
				if resolved := resolveTypeVariable(dmm, value); resolved != value {
					resolvedVars[key] = resolved
				}
			}
		}
		key := instance.Prefab().Path()
		for _, name := range keys {
			key += "\x00" + name + "=" + vars[name]
		}
		id := doc.definitions[key]
		if id == 0 {
			id = doc.nextDefinition
			doc.nextDefinition++
			doc.definitions[key] = id
			atom := api.AtomDefinition{ID: id, Path: instance.Prefab().Path(), Vars: vars, ResolvedVars: resolvedVars}
			doc.atoms[id] = atom
			doc.newDefinitions = append(doc.newDefinitions, atom)
			out.Atoms = append(out.Atoms, api.AtomInstance{ID: instance.Id(), DefinitionID: id})
			continue
		}
		out.Atoms = append(out.Atoms, api.AtomInstance{ID: instance.Id(), DefinitionID: id})
	}
	return out
}
func (m *Manager) contextTurf(dmm *dmmap.Dmm, tile *dmmap.Tile) api.ContextTurf {
	turf := api.ContextTurf{X: tile.Coord.X, Y: tile.Coord.Y, Z: tile.Coord.Z}
	for _, instance := range tile.Instances() {
		vars := make(map[string]string)
		resolvedVars := make(map[string]string)
		prefabVars := instance.Prefab().Vars()
		for _, key := range resolvedVariableNames(prefabVars) {
			if value, ok := prefabVars.Value(key); ok {
				vars[key] = value
				if resolved := resolveTypeVariable(dmm, value); resolved != value {
					resolvedVars[key] = resolved
				}
			}
		}
		turf.Atoms = append(turf.Atoms, api.AtomDefinition{Path: instance.Prefab().Path(), Vars: vars, ResolvedVars: resolvedVars})
	}
	return turf
}

// resolveTypeVariable follows a DM /type::variable reference for extension metadata.
func resolveTypeVariable(dmm *dmmap.Dmm, value string) string {
	typePath, variableName, found := strings.Cut(value, "::")
	if !found || dmm.Environment == nil {
		return value
	}
	object := dmm.Environment.Objects[typePath]
	if object == nil {
		return value
	}
	if resolved, exists := object.Vars.Value(variableName); exists {
		return resolved
	}
	return value
}

func resolvedVariableNames(vars *dmvars.Variables) []string {
	names := make(map[string]struct{})
	for current := vars; current != nil; current = current.Parent() {
		for _, name := range current.Iterate() {
			names[name] = struct{}{}
		}
	}
	keys := make([]string, 0, len(names))
	for name := range names {
		keys = append(keys, name)
	}
	return keys
}

type manifest struct {
	ID           string            `json:"id"`
	Name         string            `json:"name"`
	APIVersion   int               `json:"apiVersion"`
	Commands     map[string]string `json:"commands"`
	Capabilities []string          `json:"capabilities"`
	Actions      []manifestAction  `json:"actions"`
}
type manifestAction struct {
	ID, Menu, Label, Kind      string
	RenderPass, RenderProperty string
	Min, Max, Default          int
}
type work struct {
	message   api.Request
	immediate bool
}
type workerResult struct {
	message api.Request
	err     error
}
type client struct {
	manifest  manifest
	process   *plugin.Client
	extension api.Extension
	jobs      chan work
	results   chan workerResult
	stop      chan struct{}
}

func (c *client) hasCapability(capability string) bool {
	for _, candidate := range c.manifest.Capabilities {
		if candidate == capability {
			return true
		}
	}
	return false
}
func (c *client) usesMap() bool {
	return c.hasCapability(api.CapabilityRender) || c.hasCapability(api.CapabilityContextMenu) || c.hasCapability(api.CapabilityAppearance)
}

func (c *client) startWorker() {
	c.jobs = make(chan work, 128)
	c.results = make(chan workerResult, 128)
	c.stop = make(chan struct{})
	go c.run()
}
func (c *client) queue(work work) {
	select {
	case c.jobs <- work:
	default:
		log.Warn().Str("extension", c.manifest.ID).Msg("extension update queue full")
	}
}
func (c *client) close() {
	select {
	case <-c.stop:
	default:
		close(c.stop)
	}
	if c.process != nil {
		c.process.Kill()
	}
}
func (c *client) run() {
	pending := make(map[string]work)
	timer := time.NewTimer(time.Hour)
	if !timer.Stop() {
		<-timer.C
	}
	for {
		select {
		case <-c.stop:
			return
		case work := <-c.jobs:
			if work.immediate || work.message.Type == "map.close" {
				c.execute(work)
				continue
			}
			pending[work.message.MapID] = merge(pending[work.message.MapID], work)
			timer.Reset(50 * time.Millisecond)
		case <-timer.C:
			for id, work := range pending {
				delete(pending, id)
				c.execute(work)
			}
		}
	}
}
func merge(previous, next work) work {
	if previous.message.Type == "" {
		return next
	}
	if previous.message.Type != "map.update" || next.message.Type != "map.update" {
		return next
	}
	tiles := make(map[string]api.Tile)
	for _, tile := range previous.message.Update.Tiles {
		tiles[fmt.Sprintf("%d/%d/%d", tile.X, tile.Y, tile.Z)] = tile
	}
	for _, tile := range next.message.Update.Tiles {
		tiles[fmt.Sprintf("%d/%d/%d", tile.X, tile.Y, tile.Z)] = tile
	}
	next.message.Update.Tiles = next.message.Update.Tiles[:0]
	for _, tile := range tiles {
		next.message.Update.Tiles = append(next.message.Update.Tiles, tile)
	}
	next.message.Update.Definitions = append(previous.message.Update.Definitions, next.message.Update.Definitions...)
	return next
}
func (c *client) execute(work work) {
	message, err := c.request(work.message)
	if err != nil {
		c.results <- workerResult{message: work.message, err: err}
		return
	}
	c.results <- workerResult{message: message}
}
func (c *client) request(message api.Request) (api.Request, error) {
	response, err := c.extension.Handle(message)
	return api.Request{Version: response.Version, Type: response.Type, MapID: response.MapID, Revision: response.Revision, Render: response.Render, Appearance: response.Appearance, Reason: response.Reason}, err
}

func discover(root string) []*client {
	entries, err := os.ReadDir(root)
	if err != nil {
		return nil
	}
	clients := make([]*client, 0)
	for _, entry := range entries {
		if entry.IsDir() || filepath.Ext(entry.Name()) != ".sdmmext" {
			continue
		}
		if client := newPackageClient(filepath.Join(root, entry.Name()), filepath.Join(root, ".cache")); client != nil {
			clients = append(clients, client)
		}
	}
	sort.Slice(clients, func(i, j int) bool { return clients[i].manifest.ID < clients[j].manifest.ID })
	return clients
}
