package canvas

import (
	"log"

	"github.com/go-gl/mathgl/mgl32"

	"github.com/go-gl/gl/v3.3-core/gl"

	"github.com/SpaiR/strongdmm/internal/platform"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
)

var (
	initialized  bool
	program      uint32
	uniTransform int32
	indicesCache []uint32 // Reuse all the same buffer to avoid allocations.
)

type Canvas struct {
	bucket *bucket

	vao, vbo, ebo uint32
}

func (c *Canvas) Dispose() {
	gl.DeleteVertexArrays(1, &c.vao)
	gl.DeleteBuffers(1, &c.vbo)
	gl.DeleteBuffers(1, &c.ebo)
	log.Println("[canvas] disposed")
}

func New(dmm *dmmap.Dmm) *Canvas {
	if !initialized {
		initShaderProgram()
		initUniforms()
	}

	canvas := &Canvas{
		bucket: createBucket(dmm),
	}

	gl.UseProgram(program)
	canvas.initBuffers()
	canvas.fillArrayBuffer()
	gl.UseProgram(0)

	return canvas
}

func initShaderProgram() {
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
	if program, err = platform.NewShaderProgram(vertexShader, fragmentShader); err != nil {
		log.Fatal("[canvas] unable to create shader:", err)
	}
}

func initUniforms() {
	uniTransform = gl.GetUniformLocation(program, gl.Str("Transform\x00"))
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

func (c *Canvas) Draw(width, height float32) {
	gl.Enable(gl.BLEND)
	gl.BlendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
	gl.BlendEquation(gl.FUNC_ADD)
	gl.UseProgram(program)
	gl.BindVertexArray(c.vao)
	gl.BindBuffer(gl.ARRAY_BUFFER, c.vbo)
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, c.ebo)

	mtxTransform := mgl32.Ortho(0, width, 0, height, -1, 1)
	gl.UniformMatrix4fv(uniTransform, 1, false, &mtxTransform[0])

	gl.ActiveTexture(gl.TEXTURE0)

	var activeTexture uint32

	for _, unit := range c.bucket.Units {
		rx1 := unit.x1
		ry1 := unit.y1
		rx2 := unit.x2
		ry2 := unit.y2

		if rx1 > width || ry1 > height || rx2 < 0 || ry2 < 0 {
			continue
		}

		texture := unit.sp.Texture()

		if texture != activeTexture {
			if activeTexture != 0 && len(indicesCache) > 0 {
				gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(indicesCache)*4, gl.Ptr(indicesCache), gl.STATIC_DRAW)
				gl.DrawElements(gl.TRIANGLES, int32(len(indicesCache)), gl.UNSIGNED_INT, gl.PtrOffset(0))
				indicesCache = indicesCache[:0]
			}

			gl.BindTexture(gl.TEXTURE_2D, texture)
			activeTexture = texture
		}

		unit.pushIndices(&indicesCache)
	}

	if activeTexture != 0 && len(indicesCache) > 0 {
		gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(indicesCache)*4, gl.Ptr(indicesCache), gl.STATIC_DRAW)
		gl.DrawElements(gl.TRIANGLES, int32(len(indicesCache)), gl.UNSIGNED_INT, gl.PtrOffset(0))
	}

	indicesCache = indicesCache[:0]

	gl.BindTexture(gl.TEXTURE_2D, 0)

	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, 0)
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
	gl.UseProgram(0)
	gl.Disable(gl.BLEND)
}
