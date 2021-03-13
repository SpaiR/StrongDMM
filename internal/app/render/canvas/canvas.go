package canvas

import (
	"log"

	"github.com/go-gl/mathgl/mgl32"

	"github.com/go-gl/gl/v3.3-core/gl"

	"github.com/SpaiR/strongdmm/internal/app/dm/dmmap"
	"github.com/SpaiR/strongdmm/internal/platform"
)

type Canvas struct {
	bucket *bucket

	program uint32

	vao, vbo, ebo uint32

	uniTransform int32
	mtxTransform mgl32.Mat4
}

func New(dmm *dmmap.Dmm) *Canvas {
	canvas := &Canvas{
		bucket: createBucket(dmm),
	}

	canvas.initShaderProgram()

	gl.UseProgram(canvas.program)
	canvas.initUniforms()
	canvas.initBuffers()
	canvas.fillArrayBuffer()
	gl.UseProgram(0)

	return canvas
}

func (c *Canvas) initShaderProgram() {
	vertexShader := `
#version 330 core

uniform mat4 Transform;

layout (location = 0) in vec2 in_pos;
layout (location = 1) in vec4 in_color;
layout (location = 2) in vec2 in_texture_uv;

out vec2 frag_texture_uv;
out vec4 frag_color;

void main() {
	frag_texture_uv = in_texture_uv;
	frag_color = in_color;
    gl_Position = Transform * vec4(in_pos, 1, 1);
}
` + "\x00"

	fragmentShader := `
#version 330 core

uniform sampler2D Texture;

in vec2 frag_texture_uv;
in vec4 frag_color;

out vec4 outputColor;

void main() {
    outputColor = frag_color * texture(Texture, frag_texture_uv);
}
` + "\x00"

	var err error
	if c.program, err = platform.NewShaderProgram(vertexShader, fragmentShader); err != nil {
		log.Fatal("[canvas] unable to create shader:", err)
	}
}

func (c *Canvas) initUniforms() {
	c.mtxTransform = mgl32.Ortho(0, 1280/4, 0, 768/4, -1, 1) // TODO: proper transform
	c.uniTransform = gl.GetUniformLocation(c.program, gl.Str("Transform\x00"))
	gl.UniformMatrix4fv(c.uniTransform, 1, false, &c.mtxTransform[0])
}

func (c *Canvas) initBuffers() {
	gl.GenVertexArrays(1, &c.vao)
	gl.GenBuffers(1, &c.vbo)
	gl.GenBuffers(1, &c.ebo)
}

func (c *Canvas) fillArrayBuffer() {
	gl.BindVertexArray(c.vao)

	gl.BindBuffer(gl.ARRAY_BUFFER, c.vbo)
	gl.BufferData(gl.ARRAY_BUFFER, len(c.bucket.Data)*4, gl.Ptr(c.bucket.Data), gl.STATIC_DRAW)
	c.initAttributes()
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)

	gl.BindVertexArray(0)
}

func (c *Canvas) initAttributes() {
	gl.EnableVertexAttribArray(0)
	gl.VertexAttribPointer(0, 2, gl.FLOAT, false, 8*4, gl.PtrOffset(0))

	gl.EnableVertexAttribArray(1)
	gl.VertexAttribPointer(1, 4, gl.FLOAT, false, 8*4, gl.PtrOffset(2*4))

	gl.EnableVertexAttribArray(2)
	gl.VertexAttribPointer(2, 2, gl.FLOAT, false, 8*4, gl.PtrOffset(6*4))
}

func (c *Canvas) Draw() {
	gl.Enable(gl.BLEND)
	gl.BlendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
	gl.BlendEquation(gl.FUNC_ADD)
	gl.UseProgram(c.program)
	gl.BindVertexArray(c.vao)
	gl.BindBuffer(gl.ARRAY_BUFFER, c.vbo)
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, c.ebo)

	gl.ActiveTexture(gl.TEXTURE0)

	var activeTxt uint32
	var indices []uint32

	for _, unit := range c.bucket.Units {
		txt := unit.sp.Texture()

		if txt != activeTxt {
			if activeTxt != 0 && len(indices) > 0 {
				gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(indices)*4, gl.Ptr(indices), gl.STATIC_DRAW)
				gl.DrawElements(gl.TRIANGLES, int32(len(indices)), gl.UNSIGNED_INT, gl.PtrOffset(0))
				indices = indices[:0]
			}

			gl.BindTexture(gl.TEXTURE_2D, txt)
			activeTxt = txt
		}

		indices = append(indices, unit.indices()...)
	}

	if activeTxt != 0 && len(indices) > 0 {
		gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(indices)*4, gl.Ptr(indices), gl.STATIC_DRAW)
		gl.DrawElements(gl.TRIANGLES, int32(len(indices)), gl.UNSIGNED_INT, gl.PtrOffset(0))
	}

	gl.BindTexture(gl.TEXTURE_2D, 0)

	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, 0)
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
	gl.UseProgram(0)
	gl.Disable(gl.BLEND)
}
