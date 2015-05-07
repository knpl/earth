uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;
uniform vec4 lightpos;

attribute vec4 pos;
attribute vec3 normal;
attribute vec4 color;

varying vec3 E;
varying vec3 N;
varying vec3 L;

varying vec4 fcolor;

void main() {
	mat4 modelview = view * model;
	vec4 eye = modelview * pos;
	
	N = (modelview * vec4(normal, 0.0)).xyz;
	L = (view * lightpos - eye).xyz;
	E = -eye.xyz;
	
	fcolor = color;
	gl_Position = proj * eye;
}