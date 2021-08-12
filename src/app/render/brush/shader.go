package brush

const (
	uniformLocationTransform  = 0
	uniformLocationHasTexture = 1
)

func vertexShader() string {
	return `
#version 330 core

uniform mat4 Transform;

layout (location = 0) in vec2 in_pos;
layout (location = 1) in vec4 in_color;
layout (location = 2) in vec2 in_texture_uv;

out vec2 frag_texture_uv;
out vec4 frag_color;

void main() {
	frag_texture_uv = in_texture_uv;
	frag_color = in_color;
    gl_Position = Transform * vec4(in_pos, 1, 1);
}
` + "\x00"
}

func fragmentShader() string {
	return `
#version 330 core

uniform sampler2D Texture;
uniform bool HasTexture;

in vec2 frag_texture_uv;
in vec4 frag_color;

out vec4 outputColor;

void main() {
	if (HasTexture)
    	outputColor = frag_color * texture(Texture, frag_texture_uv);
	else
		outputColor = frag_color;
}
` + "\x00"
}
