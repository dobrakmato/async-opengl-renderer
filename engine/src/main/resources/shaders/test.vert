#version 330


layout(location = 0) in vec3 position;

out vec2 texCoord0;

uniform mat4 mvp;

void main() {
    texCoord0 = position.xy;
	gl_Position = vec4(position, 1.0);
}
