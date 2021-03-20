package widget

import (
	"log"

	"github.com/go-gl/gl/v3.3-core/gl"

	"github.com/SpaiR/strongdmm/internal/app/dm/dmmap"
	"github.com/SpaiR/strongdmm/internal/app/render/canvas"
)

type CanvasAction interface {
	RunLater(job func())
}

type Canvas struct {
	action CanvasAction

	canvas *canvas.Canvas

	width, height float32

	frameBuffer uint32
	Texture     uint32
}

func (c *Canvas) Dispose() {
	// Run later so it will be cleared in the next frame.
	// Otherwise we will see a graphic artifact.
	c.action.RunLater(func() {
		c.canvas.Dispose()
		gl.DeleteFramebuffers(1, &c.frameBuffer)
		gl.DeleteTextures(1, &c.Texture)
		log.Println("[widget] canvas disposed")
	})
}

func NewCanvas(action CanvasAction, dmm *dmmap.Dmm) *Canvas {
	c := &Canvas{
		action: action,
		canvas: canvas.New(dmm),
	}
	gl.GenFramebuffers(1, &c.frameBuffer)
	return c
}

func (c *Canvas) Process(width, height float32) {
	c.updateCanvasTexture(width, height)
	gl.BindFramebuffer(gl.FRAMEBUFFER, c.frameBuffer)
	gl.Viewport(0, 0, int32(width), int32(height))
	gl.Clear(gl.COLOR_BUFFER_BIT)
	c.canvas.Draw(width, height)
	gl.BindFramebuffer(gl.FRAMEBUFFER, 0)
}

func (c *Canvas) updateCanvasTexture(width, height float32) {
	if c.width != width || c.height != height || c.Texture == 0 {
		c.width, c.height = width, height
		c.createCanvasTexture()
	}
}

func (c *Canvas) createCanvasTexture() {
	gl.DeleteTextures(1, &c.Texture)
	gl.GenTextures(1, &c.Texture)
	gl.BindTexture(gl.TEXTURE_2D, c.Texture)
	gl.TexImage2D(gl.TEXTURE_2D, 0, gl.RGB, int32(c.width), int32(c.height), 0, gl.RGB, gl.UNSIGNED_BYTE, gl.Ptr(make([]float32, int(c.width*c.height))))
	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR)
	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST)
	gl.BindTexture(gl.TEXTURE_2D, 0)

	gl.BindFramebuffer(gl.FRAMEBUFFER, c.frameBuffer)
	gl.FramebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, c.Texture, 0)
	gl.BindFramebuffer(gl.FRAMEBUFFER, 0)
}
