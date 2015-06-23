precision mediump float;

uniform sampler2D sampler;
	
varying vec3 E;
varying vec3 N;
varying vec3 L;
varying vec2 ftex;

void main() {
	vec3 NE = normalize(E);
	vec3 NN = normalize(N);
	vec3 NL = normalize(L);
	vec3 NH = normalize(NL + NE);
	
	const float shininess = 50.0;
	float diffuse = max(dot(NN, NL), 0.0);
	float specular = pow(max(dot(NN, NH), 0.0), shininess);
	gl_FragColor = (.5 + .25 * diffuse + .25 * specular) * texture2D(sampler, ftex);
}