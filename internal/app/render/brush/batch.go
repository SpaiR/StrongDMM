package brush

type modeType int

const (
	rect modeType = 0
	line modeType = 1
)

var (
	batchData  []float32
	batchCalls []batchCall

	batchMode modeType

	batchIdx     uint32
	batchIndices []uint32

	batchTexture uint32
	batchLen     int32
	batchOffset  int
)

func batchClear() {
	batchData = batchData[:0]
	batchCalls = batchCalls[:0]

	batchMode = 0

	batchIdx = 0
	batchIndices = batchIndices[:0]

	batchTexture = 0
	batchOffset = 0
	batchLen = 0
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

func RectTextured(x1, y1, x2, y2, r, g, b, a float32, texture uint32, u1, v1, u2, v2 float32) {
	if batchMode != rect || batchTexture != texture {
		batchFlush()
	}

	batchTexture = texture
	batchMode = rect

	batchData = append(batchData,
		x1, y1, r, g, b, a, u1, v2, // bottom-left
		x2, y1, r, g, b, a, u2, v2, // bottom-right
		x1, y2, r, g, b, a, u1, v1, // top-left
		x2, y2, r, g, b, a, u2, v1, // top-right
	)

	batchIndices = append(batchIndices, batchIdx+0, batchIdx+1, batchIdx+2, batchIdx+1, batchIdx+3, batchIdx+2)
	batchIdx += rectVerticesLen
	batchLen += rectIndicesLen
}

func RectFilled(x1, y1, x2, y2, r, g, b, a float32) {
	RectTextured(x1, y1, x2, y2, r, g, b, a, 0, 0, 0, 0, 0)
}

func Rect(x1, y1, x2, y2, r, g, b, a float32) {
	Line(x1, y1, x2, y1, r, g, b, a)
	Line(x2, y1, x2, y2, r, g, b, a)
	Line(x2, y2, x1, y2, r, g, b, a)
	Line(x1, y2, x1, y1, r, g, b, a)
}

func Line(x1, y1, x2, y2, r, g, b, a float32) {
	if batchMode != line {
		batchFlush()
	}

	batchTexture = 0
	batchMode = line

	batchData = append(batchData,
		x1, y1, r, g, b, a, 0, 0, // first point
		x2, y2, r, g, b, a, 0, 0, // second point
	)

	batchIndices = append(batchIndices, batchIdx+0, batchIdx+1)
	batchIdx += lineVerticesLen
	batchLen += lineIndicesLen
}

func batchFlush() {
	if batchLen != 0 && len(batchIndices) > 0 {
		batchCalls = append(batchCalls, batchCall{
			texture: batchTexture,
			len:     batchLen,
			offset:  batchOffset,
			mode:    batchMode,
		})

		var offset int
		if batchMode == rect {
			offset = rectVerticesLen
		}

		batchOffset += int(batchLen) * offset
		batchLen = 0
	}
}
