package platform

import (
	"unsafe"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/gl/v3.3-core/gl"
)

var (
	gVertHandle   uint32
	gFragHandle   uint32
	gShaderHandle uint32

	gUniformLocationTex        int32
	gUniformLocationProjMtx    int32
	gAttributeLocationVtxPos   int32
	gAttributeLocationVtxUV    int32
	gAttributeLocationVtxColor int32

	gVboHandle      uint32
	gElementsHandle uint32
	gVaoHandle      uint32

	gFontTexture uint32

	lastActiveTexture      int32
	lastProgram            int32
	lastTexture            int32
	lastSampler            int32
	lastArrayBuffer        int32
	lastVertexArrayObject  int32
	lastPolygonMode        [2]int32
	lastViewport           [4]int32
	lastScissorBox         [4]int32
	lastBlendSrcRgb        int32
	lastBlendDstRgb        int32
	lastBlendSrcAlpha      int32
	lastBlendDstAlpha      int32
	lastBlendEquationRgb   int32
	lastBlendEquationAlpha int32
	lastEnableBlend        bool
	lastEnableCullFace     bool
	lastEnableDepthTest    bool
	lastEnableScissorTest  bool
)

func InitImGuiGL() {
	createDeviceObjects()
}

func UpdateFontsTexture() {
	gl.DeleteTextures(1, &gFontTexture)

	fontAtlas := imgui.CurrentIO().Fonts()
	texture := fontAtlas.TextureDataRGBA32()

	gl.GenTextures(1, &gFontTexture)
	gl.BindTexture(gl.TEXTURE_2D, gFontTexture)
	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR)
	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR)
	gl.TexImage2D(gl.TEXTURE_2D, 0, gl.RGBA, int32(texture.Width), int32(texture.Height), 0, gl.RGBA, gl.UNSIGNED_BYTE, texture.Pixels)

	fontAtlas.SetTextureID(imgui.TextureID(gFontTexture))
}

func Render(drawData imgui.DrawData) {
	if len(drawData.CommandLists()) <= 0 {
		return
	}

	displaySize := drawData.GetDisplaySize()
	fbScale := drawData.GetFramebufferScale()

	fbWidth := displaySize.X * fbScale.X
	fbHeight := displaySize.Y * fbScale.Y

	if fbWidth <= 0 || fbHeight <= 0 {
		return
	}

	displayPos := drawData.GetDisplayPos()

	backupGlState()
	bind(&displayPos, &displaySize, int32(fbWidth), int32(fbHeight))

	for _, list := range drawData.CommandLists() {
		var indexBufferOffset uintptr

		vertexBuffer, vertexBufferSize := list.VertexBuffer()
		indexBuffer, indexBufferSize := list.IndexBuffer()
		gl.BufferData(gl.ARRAY_BUFFER, vertexBufferSize, vertexBuffer, gl.STREAM_DRAW)
		gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, indexBufferSize, indexBuffer, gl.STREAM_DRAW)

		indexSize := imgui.IndexBufferLayout()

		for _, cmd := range list.Commands() {
			clipRect := cmd.ClipRect()

			clipRectX := (clipRect.X - displayPos.X) * fbScale.X
			clipRectY := (clipRect.Y - displayPos.Y) * fbScale.Y
			clipRectZ := (clipRect.Z - displayPos.X) * fbScale.X
			clipRectW := (clipRect.W - displayPos.Y) * fbScale.Y

			if cmd.HasUserCallback() {
				cmd.CallUserCallback(list)
			} else if clipRectX < fbWidth && clipRectY < fbHeight && clipRectZ > 0 && clipRectW > 0 {
				gl.Scissor(int32(clipRectX), int32(fbHeight-clipRectW), int32(clipRectZ-clipRectX), int32(clipRectW-clipRectY))
				gl.BindTexture(gl.TEXTURE_2D, uint32(cmd.TextureID()))
				gl.DrawElements(gl.TRIANGLES, int32(cmd.ElementCount()), uint32(gl.UNSIGNED_SHORT), unsafe.Pointer(indexBufferOffset))
			}

			indexBufferOffset += uintptr(cmd.ElementCount() * indexSize)
		}
	}

	unbind()
	restoreModifiedState()
}

func DisposeImGuiGL() {
	gl.DeleteBuffers(1, &gVboHandle)
	gl.DeleteBuffers(1, &gElementsHandle)
	gl.DetachShader(gShaderHandle, gVertHandle)
	gl.DetachShader(gShaderHandle, gFragHandle)
	gl.DeleteProgram(gShaderHandle)
	gl.DeleteTextures(1, &gFontTexture)
}

func createDeviceObjects() {
	// Backup GL state
	gl.GetIntegerv(gl.TEXTURE_BINDING_2D, &lastTexture)
	gl.GetIntegerv(gl.ARRAY_BUFFER_BINDING, &lastArrayBuffer)
	gl.GetIntegerv(gl.VERTEX_ARRAY_BINDING, &lastVertexArrayObject)

	createShaders()

	gUniformLocationTex = gl.GetUniformLocation(gShaderHandle, gl.Str("Texture\x00"))
	gUniformLocationProjMtx = gl.GetUniformLocation(gShaderHandle, gl.Str("ProjMtx\x00"))
	gAttributeLocationVtxPos = gl.GetAttribLocation(gShaderHandle, gl.Str("Position\x00"))
	gAttributeLocationVtxUV = gl.GetAttribLocation(gShaderHandle, gl.Str("UV\x00"))
	gAttributeLocationVtxColor = gl.GetAttribLocation(gShaderHandle, gl.Str("Color\x00"))

	// Create buffers
	gl.GenBuffers(1, &gVboHandle)
	gl.GenBuffers(1, &gElementsHandle)

	UpdateFontsTexture()

	// Restore modified GL state
	gl.BindTexture(gl.TEXTURE_2D, uint32(lastTexture))
	gl.BindBuffer(gl.ARRAY_BUFFER, uint32(lastArrayBuffer))
	gl.BindVertexArray(uint32(lastVertexArrayObject))
}

func createShaders() {
	createAndCompileShader := func(t uint32, source string) uint32 {
		id := gl.CreateShader(t)

		cSource, free := gl.Strs(source + "\x00")
		defer free()

		gl.ShaderSource(id, 1, cSource, nil)
		gl.CompileShader(id)

		return id
	}

	vertShaderSource := `
#version 150
uniform mat4 ProjMtx;
in vec2 Position;
in vec2 UV;
in vec4 Color;
out vec2 Frag_UV;
out vec4 Frag_Color;
void main()
{
	Frag_UV = UV;
	Frag_Color = Color;
	gl_Position = ProjMtx * vec4(Position.xy,0,1);
}
`
	fragShaderSource := `
#version 150
uniform sampler2D Texture;
in vec2 Frag_UV;
in vec4 Frag_Color;
out vec4 Out_Color;
void main()
{
	Out_Color = Frag_Color * texture(Texture, Frag_UV.st);
}
`
	gVertHandle = createAndCompileShader(gl.VERTEX_SHADER, vertShaderSource)
	gFragHandle = createAndCompileShader(gl.FRAGMENT_SHADER, fragShaderSource)

	gShaderHandle = gl.CreateProgram()
	gl.AttachShader(gShaderHandle, gVertHandle)
	gl.AttachShader(gShaderHandle, gFragHandle)
	gl.LinkProgram(gShaderHandle)
}

func backupGlState() {
	gl.GetIntegerv(gl.ACTIVE_TEXTURE, &lastActiveTexture)
	gl.ActiveTexture(gl.TEXTURE0)
	gl.GetIntegerv(gl.CURRENT_PROGRAM, &lastProgram)
	gl.GetIntegerv(gl.TEXTURE_BINDING_2D, &lastTexture)
	gl.GetIntegerv(gl.SAMPLER_BINDING, &lastSampler)
	gl.GetIntegerv(gl.ARRAY_BUFFER_BINDING, &lastArrayBuffer)
	gl.GetIntegerv(gl.VERTEX_ARRAY_BINDING, &lastVertexArrayObject)
	gl.GetIntegerv(gl.POLYGON_MODE, &lastPolygonMode[0])
	gl.GetIntegerv(gl.VIEWPORT, &lastViewport[0])
	gl.GetIntegerv(gl.SCISSOR_BOX, &lastScissorBox[0])
	gl.GetIntegerv(gl.BLEND_SRC_RGB, &lastBlendSrcRgb)
	gl.GetIntegerv(gl.BLEND_DST_RGB, &lastBlendDstRgb)
	gl.GetIntegerv(gl.BLEND_SRC_ALPHA, &lastBlendSrcAlpha)
	gl.GetIntegerv(gl.BLEND_DST_ALPHA, &lastBlendDstAlpha)
	gl.GetIntegerv(gl.BLEND_EQUATION_RGB, &lastBlendEquationRgb)
	gl.GetIntegerv(gl.BLEND_EQUATION_ALPHA, &lastBlendEquationAlpha)
	lastEnableBlend = gl.IsEnabled(gl.BLEND)
	lastEnableCullFace = gl.IsEnabled(gl.CULL_FACE)
	lastEnableDepthTest = gl.IsEnabled(gl.DEPTH_TEST)
	lastEnableScissorTest = gl.IsEnabled(gl.SCISSOR_TEST)
}

func restoreModifiedState() {
	gl.UseProgram(uint32(lastProgram))
	gl.BindTexture(gl.TEXTURE_2D, uint32(lastTexture))
	gl.BindSampler(0, uint32(lastSampler))
	gl.ActiveTexture(uint32(lastActiveTexture))
	gl.BindVertexArray(uint32(lastVertexArrayObject))
	gl.BindBuffer(gl.ARRAY_BUFFER, uint32(lastArrayBuffer))
	gl.BlendEquationSeparate(uint32(lastBlendEquationRgb), uint32(lastBlendEquationAlpha))
	gl.BlendFuncSeparate(uint32(lastBlendSrcRgb), uint32(lastBlendDstRgb), uint32(lastBlendSrcAlpha), uint32(lastBlendDstAlpha))
	toggleGlMode(gl.BLEND, lastEnableBlend)
	toggleGlMode(gl.CULL_FACE, lastEnableCullFace)
	toggleGlMode(gl.DEPTH_TEST, lastEnableDepthTest)
	toggleGlMode(gl.SCISSOR_TEST, lastEnableScissorTest)
	gl.PolygonMode(gl.FRONT_AND_BACK, uint32(lastPolygonMode[0]))
	gl.Viewport(lastViewport[0], lastViewport[1], lastViewport[2], lastViewport[3])
	gl.Scissor(lastScissorBox[0], lastScissorBox[1], lastScissorBox[2], lastScissorBox[3])
}

func toggleGlMode(mode uint32, state bool) {
	if state {
		gl.Enable(mode)
	} else {
		gl.Disable(mode)
	}
}

func bind(displayPos, displaySize *imgui.Vec2, fbWidth, fbHeight int32) {
	gl.GenVertexArrays(1, &gVaoHandle)

	gl.Enable(gl.BLEND)
	gl.BlendEquation(gl.FUNC_ADD)
	gl.BlendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
	gl.Disable(gl.CULL_FACE)
	gl.Disable(gl.DEPTH_TEST)
	gl.Enable(gl.SCISSOR_TEST)
	gl.PolygonMode(gl.FRONT_AND_BACK, gl.FILL)

	// Setup viewport, orthographic projection matrix
	// Our visible imgui space lies from draw_data->DisplayPos (top left) to draw_data->DisplayPos+data_data->DisplaySize (bottom right).
	// DisplayMin is typically (0,0) for single viewport apps.
	gl.Viewport(0, 0, fbWidth, fbHeight)

	left := displayPos.X
	right := displayPos.X + displaySize.X
	top := displayPos.Y
	bottom := displayPos.Y + displaySize.Y

	orthoProjMatrix := [4 * 4]float32{}
	orthoProjMatrix[0] = 2.0 / (right - left)
	orthoProjMatrix[5] = 2.0 / (top - bottom)
	orthoProjMatrix[10] = -1.0
	orthoProjMatrix[12] = (right + left) / (left - right)
	orthoProjMatrix[13] = (top + bottom) / (bottom - top)
	orthoProjMatrix[15] = 1.0

	gl.UseProgram(gShaderHandle)
	gl.Uniform1i(gUniformLocationTex, 0)
	gl.UniformMatrix4fv(gUniformLocationProjMtx, 1, false, &orthoProjMatrix[0])
	gl.BindSampler(0, 0)

	gl.BindVertexArray(gVaoHandle)

	gl.BindBuffer(gl.ARRAY_BUFFER, gVboHandle)
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, gElementsHandle)
	gl.EnableVertexAttribArray(uint32(gAttributeLocationVtxPos))
	gl.EnableVertexAttribArray(uint32(gAttributeLocationVtxUV))
	gl.EnableVertexAttribArray(uint32(gAttributeLocationVtxColor))

	vertexSize, vertexOffsetPos, vertexOffsetUv, vertexOffsetCol := imgui.VertexBufferLayout()
	gl.VertexAttribPointer(uint32(gAttributeLocationVtxPos), 2, gl.FLOAT, false, int32(vertexSize), unsafe.Pointer(uintptr(vertexOffsetPos)))
	gl.VertexAttribPointer(uint32(gAttributeLocationVtxUV), 2, gl.FLOAT, false, int32(vertexSize), unsafe.Pointer(uintptr(vertexOffsetUv)))
	gl.VertexAttribPointer(uint32(gAttributeLocationVtxColor), 4, gl.UNSIGNED_BYTE, true, int32(vertexSize), unsafe.Pointer(uintptr(vertexOffsetCol)))
}

func unbind() {
	gl.DeleteVertexArrays(1, &gVaoHandle)
}
