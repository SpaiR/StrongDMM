package program

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/platform"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/mathgl/mgl32"
)

type program struct {
	attributes
	program       uint32
	vao, vbo, ebo uint32
}

func (p *program) initShader(vertex, fragment string) {
	log.Println("[program] initializing shader...")
	var err error
	if p.program, err = platform.NewShaderProgram(vertex, fragment); err != nil {
		log.Fatal("[program] unable to create shader:", err)
	}
	log.Println("[program] shader initialized")
}

func (p *program) initBuffers() {
	log.Println("[program] initializing buffers...")
	gl.GenVertexArrays(1, &p.vao)
	gl.GenBuffers(1, &p.vbo)
	gl.GenBuffers(1, &p.ebo)
	log.Println("[program] buffers initialized")
}

func (p *program) initAttributes() {
	gl.BindVertexArray(p.vao)
	gl.BindBuffer(gl.ARRAY_BUFFER, p.vbo)

	var offset int32
	for idx, attr := range p.attrs {
		gl.EnableVertexAttribArray(uint32(idx))
		gl.VertexAttribPointer(uint32(idx), attr.size, attr.xtype, attr.normalized, p.attributes.stride, gl.PtrOffset(int(offset)))
		offset += attr.size * attr.xtypeSize
	}

	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
}

func (p *program) Prepare(mtxTransform mgl32.Mat4) {
	gl.UseProgram(p.program)
	gl.BindVertexArray(p.vao)
	gl.BindBuffer(gl.ARRAY_BUFFER, p.vbo)
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, p.ebo)
	gl.UniformMatrix4fv(0, 1, false, &mtxTransform[0]) // Every shader transform matrix is positioned in zero location
}

func (p *program) Cleanup() {
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, 0)
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
	gl.UseProgram(0)
}

func (p *program) Dispose() {
	log.Println("[program] disposing...")
	gl.DeleteVertexArrays(1, &p.vao)
	gl.DeleteBuffers(1, &p.vbo)
	gl.DeleteBuffers(1, &p.ebo)
	log.Println("[program] disposed")
}

func (p *program) SetArrayBuffer(data []float32) {
	gl.BindBuffer(gl.ARRAY_BUFFER, p.vbo)
	gl.BufferData(gl.ARRAY_BUFFER, len(data)*4, gl.Ptr(data), gl.STATIC_DRAW)
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
}

func (p *program) UpdateElementArrayBuffer(data []uint32) {
	gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(data)*4, gl.Ptr(data), gl.STATIC_DRAW)
}

type attribute struct {
	size       int32
	xtype      uint32
	xtypeSize  int32
	normalized bool
}

type attributes struct {
	stride int32
	attrs  []attribute
}

func (a *attributes) addAttribute(attribute attribute) {
	a.attrs = append(a.attrs, attribute)
	a.stride += attribute.xtypeSize * attribute.size
}
