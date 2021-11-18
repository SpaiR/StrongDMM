package brush

type modeType int

const (
	mtRect modeType = iota
	mtLine
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
