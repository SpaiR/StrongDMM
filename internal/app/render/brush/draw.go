package brush

import (
	"sdmm/internal/platform"

	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/mathgl/mgl32"
)

func Draw(w, h, x, y, z float32) {
	// Ensure that the latest batch state is persisted.
	batching.flush()

	// No data to draw.
	if len(batching.data) == 0 {
		return
	}

	gl.UseProgram(program)
	gl.BindVertexArray(vao)

	mtxTransform := transformationMatrix(w, h, x, y, z)
	gl.UniformMatrix4fv(uniformLocationTransform, 1, false, &mtxTransform[0])

	gl.BindBuffer(gl.ARRAY_BUFFER, vbo)
	gl.BufferData(gl.ARRAY_BUFFER, len(batching.data)*platform.FloatSize, gl.Ptr(batching.data), gl.STREAM_DRAW)
	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, ebo)
	gl.BufferData(gl.ELEMENT_ARRAY_BUFFER, len(batching.indices)*platform.FloatSize, gl.Ptr(batching.indices), gl.STREAM_DRAW)

	for _, c := range batching.calls {
		if c.texture != 0 {
			gl.Uniform1i(uniformLocationHasTexture, 1)
			gl.BindTexture(gl.TEXTURE_2D, c.texture)
		} else {
			gl.Uniform1i(uniformLocationHasTexture, 0)
		}

		switch c.mode {
		case mtRect:
			gl.DrawElementsWithOffset(gl.TRIANGLES, c.len, gl.UNSIGNED_INT, uintptr(c.offset))
		case mtLine:
			gl.DrawElementsWithOffset(gl.LINES, c.len, gl.UNSIGNED_INT, uintptr(c.offset))
		}
	}

	gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, 0)
	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
	gl.UseProgram(0)

	// Clear batch state.
	batching.clear()
}

func transformationMatrix(w, h, x, y, z float32) mgl32.Mat4 {
	view := mgl32.Ortho(0, w, 0, h, -1, 1)
	scale := mgl32.Scale2D(z, z).Mat4()
	shift := mgl32.Translate2D(x, y).Mat4()
	return view.Mul4(scale).Mul4(shift)
}
