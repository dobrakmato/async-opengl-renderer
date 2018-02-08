#version 330

uniform sampler2D image;

in vec2 texCoord0;

void main() {
	// gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	gl_FragColor = vec4(texture(image, texCoord0).rgb, 1);
}
