precision mediump float;

uniform sampler2D texsamp;
uniform sampler2D normsamp;
uniform sampler2D specsamp;

varying vec3 N;
varying vec3 T;
varying vec3 E;
varying vec3 L;
varying vec2 ftex;

void main() {

	vec3 NT = normalize(T);
	vec3 NN = normalize(N);
	mat3 tbn = mat3(NT, cross(NT, NN), NN);
	
	NN = tbn * (2.0 * vec3(texture2D(normsamp, ftex)) - 1.0);
	vec3 NE = normalize(E);
	vec3 NL = normalize(L);
	vec3 NH = normalize(NL + NE);
	
	float diff = max(dot(NN, NL), 0.0);
	vec3 diffuse  = (.2 + .6 * diff) * vec3(texture2D(texsamp, ftex));
	
	float spec = pow(max(dot(NN, NH), 0.0), 20.0);
	vec3 specular = .2 * spec * vec3(texture2D(specsamp, ftex));
	
	gl_FragColor = vec4(diffuse + specular, 1.0);
}