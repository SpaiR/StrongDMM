package brush

import (
	"github.com/SpaiR/strongdmm/pkg/platform"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/mathgl/mgl32"
)

func Draw(w, h, x, y, z float32) {
	// Ensure that the latest batch state is persisted.
	batchFlush()

	gl.UseProgram(program)
	gl.BindVertexArray(vao)

	mtxTransform := transformationMatrix(w, h, x, y, z)
	gl.UniformMatrix4fv(uniformTransformMtxLoc, 1, false, &mtxTransform[0])

	gl.BindBuffer(gl.ARRAY_BUFFER, vbo)
	gl.BufferData(gl.ARRAY_BUFFER, len(batchData)*platform.FloatSize, gl.Ptr(batchData), gl.STATIC_DRAW)
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, ebo)
	gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(batchIndices)*platform.FloatSize, gl.Ptr(batchIndices), gl.STATIC_DRAW)

	for _, c := range batchCalls {
		if c.texture != 0 {
			gl.Uniform1i(uniformHasTextureLoc, 1)
			gl.BindTexture(gl.TEXTURE_2D, c.texture)
		} else {
			gl.Uniform1i(uniformHasTextureLoc, 0)
		}

		switch c.mode {
		case rect:
			gl.DrawElements(gl.TRIANGLES, c.len, gl.UNSIGNED_INT, gl.PtrOffset(c.offset))
		case line:
			gl.DrawElements(gl.LINES, c.len, gl.UNSIGNED_INT, gl.PtrOffset(c.offset))
		}
	}

	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, 0)
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
	gl.UseProgram(0)

	// Clear batch state.
	batchClear()
}

func transformationMatrix(w, h, x, y, z float32) mgl32.Mat4 {
	view := mgl32.Ortho(0, w, 0, h, -1, 1)
	scale := mgl32.Scale2D(z, z).Mat4()
	shift := mgl32.Translate2D(x, y).Mat4()
	return view.Mul4(scale).Mul4(shift)
}
