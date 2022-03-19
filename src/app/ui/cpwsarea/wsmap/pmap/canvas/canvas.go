package canvas

import (
	"log"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/gl/v3.3-core/gl"
	"sdmm/app/render"
	"sdmm/app/window"
)

type Color struct {
	R, G, B, A float32
}

type Canvas struct {
	render *render.Render

	width, height float32

	frameBuffer uint32
	texture     uint32

	ClearColor Color
}

func (c *Canvas) Texture() uint32 {
	return c.texture
}

func (c *Canvas) Render() *render.Render {
	return c.render
}

func (c *Canvas) ReadPixels() []byte {
	var pixels = make([]byte, int(c.width*c.height*4))
	gl.BindTexture(gl.TEXTURE_2D, c.Texture())
	gl.GetTexImage(gl.TEXTURE_2D, 0, gl.RGBA, gl.UNSIGNED_BYTE, gl.Ptr(pixels))
	gl.BindTexture(gl.TEXTURE_2D, 0)
	return pixels
}

func (c *Canvas) Dispose() {
	// Run later, so it will be cleared in the next frame.
	// Otherwise, we will see graphics artifacts.
	window.RunLater(func() {
		log.Println("[canvas] disposing...")
		gl.DeleteFramebuffers(1, &c.frameBuffer)
		gl.DeleteTextures(1, &c.texture)
		log.Println("[canvas] disposed")
	})
}

func New() *Canvas {
	c := &Canvas{render: render.New(), ClearColor: Color{.25, .25, .5, 1}}
	gl.GenFramebuffers(1, &c.frameBuffer)
	return c
}

func (c *Canvas) Process(size imgui.Vec2) {
	c.updateCanvasTexture(size.X, size.Y)
	gl.BindFramebuffer(gl.FRAMEBUFFER, c.frameBuffer)
	gl.Viewport(0, 0, int32(size.X), int32(size.Y))
	gl.ClearColor(c.ClearColor.R, c.ClearColor.G, c.ClearColor.B, c.ClearColor.A)
	gl.Clear(gl.COLOR_BUFFER_BIT)
	c.render.Draw(size.X, size.Y)
	gl.BindFramebuffer(gl.FRAMEBUFFER, 0)
}

func (c *Canvas) updateCanvasTexture(width, height float32) {
	if c.width != width || c.height != height || c.texture == 0 {
		c.width, c.height = width, height
		c.createCanvasTexture()
	}
}

func (c *Canvas) createCanvasTexture() {
	gl.DeleteTextures(1, &c.texture)
	gl.GenTextures(1, &c.texture)
	gl.BindTexture(gl.TEXTURE_2D, c.texture)
	gl.TexImage2D(gl.TEXTURE_2D, 0, gl.RGB, int32(c.width), int32(c.height), 0, gl.RGB, gl.UNSIGNED_BYTE, gl.Ptr(make([]float32, int(c.width*c.height))))
	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR)
	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST)
	gl.BindTexture(gl.TEXTURE_2D, 0)

	gl.BindFramebuffer(gl.FRAMEBUFFER, c.frameBuffer)
	gl.FramebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, c.texture, 0)
	gl.BindFramebuffer(gl.FRAMEBUFFER, 0)
}
