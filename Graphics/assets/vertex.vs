uniform mat4 view;
uniform mat4 proj;
uniform vec4 lightpos;

attribute vec4 pos;
attribute vec3 tang;
attribute vec2 tex;

varying vec3 N;
varying vec3 T;
varying vec3 B;
varying vec3 E;
varying vec3 L;
varying vec2 ftex;

void main() {
	vec4 eye = view * pos;
	
	N = (view * vec4(pos.xyz, 0.0)).xyz;
	T = (view * vec4(tang, 0.0)).xyz;
	L = (view * lightpos - eye).xyz;
	E = -eye.xyz;
	
	ftex = tex;
	gl_Position = proj * eye;
}