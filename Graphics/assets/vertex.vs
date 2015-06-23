uniform mat4 view;
uniform mat4 proj;
uniform vec4 lightpos;

attribute vec4 pos;
attribute vec2 tex;

varying vec3 E;
varying vec3 N;
varying vec3 L;

varying vec2 ftex;

void main() {
	vec4 eye = view * pos;
	
	N = (view * vec4(pos.xyz, 0.0)).xyz;
	L = (view * lightpos - eye).xyz;
	E = -eye.xyz;
	
	ftex = tex;
	gl_Position = proj * eye;
}