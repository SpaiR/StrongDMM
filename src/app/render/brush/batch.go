package brush

import "sdmm/util"

type modeType int

const (
	rect modeType = iota
	line
)

type Batching struct {
	data  []float32
	calls []batchCall

	mode modeType

	idx     uint32
	indices []uint32

	texture uint32
	len     int32
	offset  int
}

func (b *Batching) flush() {
	if b.len != 0 && len(b.indices) > 0 {
		b.calls = append(b.calls, batchCall{
			texture: b.texture,
			len:     b.len,
			offset:  b.offset,
			mode:    b.mode,
		})

		b.offset += int(b.len) * 4 // 32 bits = 4 bytes; Offset is number of bytes per buffer.
		b.len = 0
		b.texture = 0
	}
}

func (b *Batching) clear() {
	b.data = b.data[:0]
	b.calls = b.calls[:0]

	b.mode = 0

	b.idx = 0
	b.indices = b.indices[:0]

	b.texture = 0
	b.offset = 0
	b.len = 0
}

var batching *Batching

func init() {
	batching = &Batching{}
}

type batchCall struct {
	texture uint32
	len     int32
	offset  int
	mode    modeType
}

const (
	rectVerticesLen = 4 // Rect contains of 4 vertices.
	rectIndicesLen  = 6 // Rect contains of 6 indices.

	lineVerticesLen = 2 // Line contains of 2 vertices.
	lineIndicesLen  = 2 // Line contains of 2 indices.
)

func RectTextured(x1, y1, x2, y2 float32, col util.Color, texture uint32, u1, v1, u2, v2 float32) {
	RectTexturedV(x1, y1, x2, y2, col.R(), col.G(), col.B(), col.A(), texture, u1, v1, u2, v2)
}

func RectTexturedV(x1, y1, x2, y2, r, g, b, a float32, texture uint32, u1, v1, u2, v2 float32) {
	if batching.mode != rect || batching.texture != texture {
		batching.flush()
	}

	batching.texture = texture
	batching.mode = rect

	batching.data = append(batching.data,
		x1, y1, r, g, b, a, u1, v2, // bottom-left
		x2, y1, r, g, b, a, u2, v2, // bottom-right
		x1, y2, r, g, b, a, u1, v1, // top-left
		x2, y2, r, g, b, a, u2, v1, // top-right
	)

	batching.indices = append(batching.indices,
		batching.idx+0, batching.idx+1,
		batching.idx+2, batching.idx+1,
		batching.idx+3, batching.idx+2,
	)

	batching.idx += rectVerticesLen
	batching.len += rectIndicesLen
}

func RectFilled(x1, y1, x2, y2 float32, col util.Color) {
	RectFilledV(x1, y1, x2, y2, col.R(), col.G(), col.B(), col.A())
}

func RectFilledV(x1, y1, x2, y2, r, g, b, a float32) {
	RectTexturedV(x1, y1, x2, y2, r, g, b, a, 0, 0, 0, 0, 0)
}

func Rect(x1, y1, x2, y2 float32, col util.Color) {
	RectV(x1, y1, x2, y2, col.R(), col.G(), col.B(), col.A())
}

func RectV(x1, y1, x2, y2, r, g, b, a float32) {
	LineV(x1, y1, x2, y1, r, g, b, a)
	LineV(x2, y1, x2, y2, r, g, b, a)
	LineV(x2, y2, x1, y2, r, g, b, a)
	LineV(x1, y2, x1, y1, r, g, b, a)
}

func Line(x1, y1, x2, y2 float32, col util.Color) {
	LineV(x1, y1, x2, y2, col.R(), col.G(), col.B(), col.A())
}

func LineV(x1, y1, x2, y2, r, g, b, a float32) {
	if batching.mode != line {
		batching.flush()
	}

	batching.mode = line

	batching.data = append(batching.data,
		x1, y1, r, g, b, a, 0, 0, // first point
		x2, y2, r, g, b, a, 0, 0, // second point
	)

	batching.indices = append(batching.indices,
		batching.idx+0, batching.idx+1,
	)

	batching.idx += lineVerticesLen
	batching.len += lineIndicesLen
}
