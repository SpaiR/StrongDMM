# StrongDMM extensions

Extensions are Go executables managed by StrongDMM through HashiCorp
go-plugin's versioned net/rpc transport. They run out of process, so an
extension failure cannot mangle the editor.

```json
{
  "id": "example.extension",
  "name": "Example Extension",
  "apiVersion": 1,
  "capabilities": ["render", "context-menu"],
  "commands": {
    "windows-amd64": "bin/windows-amd64/example.exe"
  }
}
```

Build each command for its target platform:

```text
go build -o bin/windows-amd64/example.exe .
```

Implement `extensionapi.Extension` and call `plugin.Serve` with
`extensionapi.HandshakeConfig` and an `extensionapi.Plugin`. The API exposes
versioned `Request` values and request-shaped responses. Rendering receives map
open/update/close operations, revisions, settings, and render-pass output. The
declared capabilities determine which operations an extension receives:
`render` receives map lifecycle operations and `context-menu` receives requests
for dynamic actions at a turf. Standard output is reserved for go-plugin's
handshake and transport; write extension logs to standard error.

Context-menu requests provide the clicked turf and its atom definitions. Return
actions with stable extension-local IDs; StrongDMM returns the selected ID in a
context action request. Extensions can ignore request types outside their
declared capabilities.

`RenderPass` IDs are globally unique. Passes can be placed relative to
`core.map`, `core.selection`, and `core.overlays`; invalid dependencies and
cycles are logged. `mesh` commands support RGBA vertex colors; `bilinear`
commands are intentionally opaque and support normal, multiply, or additive
blending.

Package prebuilt commands with:

```text
cd tools/sdmmpack
go run . -source path/to/commands -manifest extension.json -output example.sdmmext
```

Extension API versions must match the host. Build and package extensions only
from trusted source.
