uniform mat4 view;
uniform mat4 proj;
uniform vec4 lightpos;

attribute vec4 pos;
attribute vec3 tang;
attribute vec2 tex;

varying vec3 vN;
varying vec3 vT;
varying vec3 vL;
varying vec3 vE;
varying vec2 vtex;

void main() {
	vec4 eye = view * pos;
	
	vN = (view * vec4(pos.xyz, 0.0)).xyz;
	vT = (view * vec4(tang, 0.0)).xyz;
	vL = (view * (lightpos - pos)).xyz;
	vE = -eye.xyz;
	vtex = tex;
	
	gl_Position = proj * eye;
}