package program

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/platform"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/mathgl/mgl32"
)

// These variables are used to batch our rendering pipeline.
var (
	batchIndices []uint32
	batchCalls   []batchCall
	batchTexture uint32
	batchOffset  int
	batchLen     int32
)

// program is an abstraction layer around low-level OpenGL stuff. Like work with shaders etc.
type program struct {
	attributes

	program       uint32
	vao, vbo, ebo uint32

	data     []float32
	dataBind bool

	mtxTransform mgl32.Mat4
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

// batchPersist will create a batchCall with proper values.
func batchPersist() {
	if batchLen != 0 && len(batchIndices) > 0 {
		batchCalls = append(batchCalls, batchCall{
			texture: batchTexture,
			len:     batchLen,
			offset:  batchOffset,
		})

		batchOffset += int(batchLen) * platform.FloatSize
		batchLen = 0
	}
}

// BatchFlush will render our batched state.
func (p *program) BatchFlush() {
	// Ensure that the latest batch state is persisted.
	batchPersist()

	gl.UseProgram(p.program)
	gl.BindVertexArray(p.vao)

	// Every shader transform matrix is positioned in zero location.
	gl.UniformMatrix4fv(0, 1, false, &p.mtxTransform[0])

	gl.BindBuffer(gl.ARRAY_BUFFER, p.vbo)

	// Data may not change, so no reason to update it.
	if !p.dataBind {
		gl.BufferData(gl.ARRAY_BUFFER, len(p.data)*platform.FloatSize, gl.Ptr(p.data), gl.STATIC_DRAW)
		p.dataBind = true
	}

	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, p.ebo)
	gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(batchIndices)*platform.FloatSize, gl.Ptr(batchIndices), gl.STATIC_DRAW)

	for _, b := range batchCalls {
		if b.texture != 0 {
			gl.BindTexture(gl.TEXTURE_2D, b.texture)
		}
		gl.DrawElements(gl.TRIANGLES, b.len, gl.UNSIGNED_INT, gl.PtrOffset(b.offset))
	}

	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, 0)
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
	gl.UseProgram(0)

	// Clear batch state.
	batchIndices = batchIndices[:0]
	batchCalls = batchCalls[:0]
	batchTexture = 0
	batchOffset = 0
	batchLen = 0
}

func (p *program) Dispose() {
	log.Println("[program] disposing...")
	gl.DeleteVertexArrays(1, &p.vao)
	gl.DeleteBuffers(1, &p.vbo)
	gl.DeleteBuffers(1, &p.ebo)
	log.Println("[program] disposed")
}

func (p *program) UpdateData(data []float32) {
	p.data = data
	p.dataBind = false
}

func (p *program) UpdateTransform(transform mgl32.Mat4) {
	p.mtxTransform = transform
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

type batchCall struct {
	texture uint32
	len     int32
	offset  int
}
