package program

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/platform"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/mathgl/mgl32"
)

const (
	rectVerticesOffset = 4 // Every rect contains of 4 vertices.
	rectIndicesLen     = 6 // Every rect contains of 6 indices.
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

	data []float32

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

		batchOffset += int(batchLen) * rectVerticesOffset
		batchLen = 0
	}
}

// BatchTexture will persist our batched data, if the texture is different from the currently batched.
func (*program) BatchTexture(texture uint32) {
	if texture != batchTexture {
		batchPersist()
		batchTexture = texture
	}
}

// BatchRect will add indices of the rect by its specific idx.
// rectIdx is a natural order of the rect in data array. It is not the index of the rect vertices themself.
func (*program) BatchRect(rectIdx int) {
	// So we convert our "natural order" index to the vertices index by applying offset of rect vertices.
	vtxIdx := uint32(rectIdx * rectVerticesOffset)
	// With these indices we create two triangles of vertices:
	// 2 3
	// 0 1
	batchIndices = append(batchIndices, vtxIdx+0, vtxIdx+1, vtxIdx+2, vtxIdx+1, vtxIdx+3, vtxIdx+2)
	batchLen += rectIndicesLen
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
	gl.BufferData(gl.ARRAY_BUFFER, len(p.data)*platform.FloatSize, gl.Ptr(p.data), gl.STATIC_DRAW)
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, p.ebo)
	gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(batchIndices)*platform.FloatSize, gl.Ptr(batchIndices), gl.STATIC_DRAW)

	for _, b := range batchCalls {
		gl.BindTexture(gl.TEXTURE_2D, b.texture)
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

func (p *program) SetData(data []float32) {
	p.data = data
}

func (p *program) UpdateTransform(w, h, x, y, z float32) {
	view := mgl32.Ortho(0, w, 0, h, -1, 1)
	scale := mgl32.Scale2D(z, z).Mat4()
	shift := mgl32.Translate2D(x, y).Mat4()
	p.mtxTransform = view.Mul4(scale).Mul4(shift)
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
