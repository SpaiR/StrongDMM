# StrongDMM extensions

Extensions are Go executables via go-plugin to extend the editor in different ways.

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

Build and package an extension with StrongDMM's generic Task:

```text
task --taskfile path/to/StrongDMM/Taskfile.yml build-extension EXTENSION_DIR=path/to/extension EXTENSION_OUTPUT=path/to/extension/dist/extension.sdmmext
```

The task cross-builds every command declared in `extension.json` and packages
the resulting `.sdmmext` archive.

Extension API versions must match the host. Build and package extensions only
from trusted source.
