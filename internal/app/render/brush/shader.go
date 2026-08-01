package brush

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
	if (HasTexture) {
		outputColor = frag_color * texture(Texture, frag_texture_uv);
	} else {
		outputColor = frag_color;
	}
}
` + "\x00"
}

func bilinearVertexShader() string {
	return `
#version 330 core
uniform mat4 Transform;
layout (location = 0) in vec2 in_pos;
layout (location = 1) in vec3 in_south_west;
layout (location = 2) in vec3 in_south_east;
layout (location = 3) in vec3 in_north_west;
layout (location = 4) in vec3 in_north_east;
layout (location = 5) in vec2 in_uv;
flat out vec3 south_west;
flat out vec3 south_east;
flat out vec3 north_west;
flat out vec3 north_east;
out vec2 uv;
void main() {
	south_west = in_south_west;
	south_east = in_south_east;
	north_west = in_north_west;
	north_east = in_north_east;
	uv = in_uv;
	gl_Position = Transform * vec4(in_pos, 1, 1);
}
` + "\x00"
}

func bilinearFragmentShader() string {
	return `
#version 330 core
flat in vec3 south_west;
flat in vec3 south_east;
flat in vec3 north_west;
flat in vec3 north_east;
in vec2 uv;
out vec4 outputColor;
void main() {
	vec3 south = mix(south_west, south_east, uv.x);
	vec3 north = mix(north_west, north_east, uv.x);
	outputColor = vec4(mix(south, north, uv.y), 1.0);
}
` + "\x00"
}
