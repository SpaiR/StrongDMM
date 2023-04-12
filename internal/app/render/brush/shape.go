package brush

import "sdmm/internal/util"

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
	if batching.mode != mtRect || batching.texture != texture {
		batching.flush()
	}

	batching.texture = texture
	batching.mode = mtRect

	batching.data = append(batching.data,
		x1, y1, r, g, b, a, u1, v2, // bottom-left
		x2, y1, r, g, b, a, u2, v2, // bottom-right
		x1, y2, r, g, b, a, u1, v1, // top-left
		x2, y2, r, g, b, a, u2, v1, // top-right
	)

	batching.indices = append(batching.indices,
		batching.idx+0, batching.idx+1, batching.idx+2, // bottom-left triangle
		batching.idx+1, batching.idx+3, batching.idx+2, // top-right triangle
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
	if batching.mode != mtLine {
		batching.flush()
	}

	batching.mode = mtLine

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
