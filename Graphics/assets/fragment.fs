precision mediump float;
	
varying vec3 E;
varying vec3 N;
varying vec3 L;

varying vec4 fcolor;

void main() {
	vec3 NE = normalize(E);
	vec3 NN = normalize(N);
	vec3 NL = normalize(L);
	vec3 NH = normalize(NL + NE);
	
	const float shininess = 20.0;
	float diffuse = max(dot(NN, NL), 0.0);
	float specular = pow(max(dot(NN, NH), 0.0), shininess);
	gl_FragColor = vec4((.3 + .35 * diffuse + .35 * specular) * (fcolor.rgb), 1.0);
}