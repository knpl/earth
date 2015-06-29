precision mediump float;

uniform sampler2D texsamp;
uniform sampler2D normsamp;
uniform sampler2D specsamp;

varying vec3 vN;
varying vec3 vT;
varying vec3 vE;
varying vec3 vL;
varying vec2 vtex;

vec3 getNormal() {
	vec3 N = normalize(vN);
	vec3 T = normalize(vT - dot(N, vT) * N);
	vec3 B = cross(T, N);
	vec3 norm = 2.0 * vec3(texture2D(normsamp, vtex)) - 1.0;
	return mat3(T, B, N) * norm;
}

float attenuation(float a, float b, float c) {
	float d = length(vL);
	return 1.0 / (a + b*d + c*d*d);
}

void main() {
	vec3 N = getNormal();
	vec3 E = normalize(vE);
	vec3 L = normalize(vL);
	vec3 H = normalize(L + E);
	
	float att = attenuation(1.0, .1, .01);
	
	float kd = max(dot(N, L), 0.0);
	vec3 diffuse  = att * (.125 + .75 * kd) * vec3(texture2D(texsamp, vtex));
	
	float ks = pow(max(dot(N, H), 0.0), 20.0);
	vec3 specular = att * .25 * ks * vec3(texture2D(specsamp, vtex));
	
	gl_FragColor = vec4(diffuse + specular, 1.0);
}