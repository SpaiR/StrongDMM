package render

import "github.com/go-gl/gl/v3.3-core/gl"

// levelTarget stores one transparent framebuffer used to compose a map Z level.
type levelTarget struct {
	framebuffer uint32
	texture     uint32
	width       int32
	height      int32
}

// bind creates or resizes the target and makes it the current render destination.
func (target *levelTarget) bind(width, height int32) {
	if target.framebuffer == 0 {
		gl.GenFramebuffers(1, &target.framebuffer)
		gl.GenTextures(1, &target.texture)
	}
	if target.width != width || target.height != height {
		target.width, target.height = width, height
		gl.BindTexture(gl.TEXTURE_2D, target.texture)
		gl.TexImage2D(gl.TEXTURE_2D, 0, gl.RGBA, width, height, 0, gl.RGBA, gl.UNSIGNED_BYTE, nil)
		gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR)
		gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST)
		gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE)
		gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE)
	}
	gl.BindFramebuffer(gl.FRAMEBUFFER, target.framebuffer)
	gl.FramebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, target.texture, 0)
	gl.Viewport(0, 0, width, height)
}
