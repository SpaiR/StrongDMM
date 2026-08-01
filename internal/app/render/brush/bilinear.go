package brush

import (
	"sdmm/internal/platform"

	"github.com/go-gl/gl/v3.3-core/gl"
)

type Color struct{ R, G, B float32 }

type BilinearQuad struct {
	X1, Y1, X2, Y2                             float32
	SouthWest, SouthEast, NorthWest, NorthEast Color
}

func BilinearQuads(quads []BilinearQuad, w, h, x, y, z float32) {
	if len(quads) == 0 {
		return
	}
	data := make([]float32, 0, len(quads)*4*16)
	indices := make([]uint32, 0, len(quads)*6)
	for quadIndex, quad := range quads {
		base := uint32(quadIndex * 4)
		for _, vertex := range [][4]float32{{quad.X1, quad.Y1, 0, 0}, {quad.X2, quad.Y1, 1, 0}, {quad.X1, quad.Y2, 0, 1}, {quad.X2, quad.Y2, 1, 1}} {
			data = append(data, vertex[0], vertex[1],
				quad.SouthWest.R, quad.SouthWest.G, quad.SouthWest.B,
				quad.SouthEast.R, quad.SouthEast.G, quad.SouthEast.B,
				quad.NorthWest.R, quad.NorthWest.G, quad.NorthWest.B,
				quad.NorthEast.R, quad.NorthEast.G, quad.NorthEast.B,
				vertex[2], vertex[3])
		}
		indices = append(indices, base, base+1, base+2, base+1, base+3, base+2)
	}
	gl.UseProgram(bilinearProgram)
	gl.BindVertexArray(bilinearVAO)
	mtx := transformationMatrix(w, h, x, y, z)
	gl.UniformMatrix4fv(bilinearTransform, 1, false, &mtx[0])
	gl.BindBuffer(gl.ARRAY_BUFFER, bilinearVBO)
	gl.BufferData(gl.ARRAY_BUFFER, len(data)*platform.FloatSize, gl.Ptr(data), gl.STREAM_DRAW)
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, bilinearEBO)
	gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(indices)*4, gl.Ptr(indices), gl.STREAM_DRAW)
	gl.DrawElements(gl.TRIANGLES, int32(len(indices)), gl.UNSIGNED_INT, gl.PtrOffset(0))
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, 0)
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
	gl.UseProgram(0)
}
