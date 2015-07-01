precision mediump float;

uniform sampler2D texsamp;
uniform sampler2D normsamp;
uniform sampler2D specsamp;

uniform float Ka;
uniform float Kd;
uniform float Ks;
uniform float shininess;

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

void main() {
	vec3 N = getNormal();
	vec3 E = normalize(vE);
	vec3 L = normalize(vL);
	vec3 H = normalize(L + E);
	
	float d = length(vL);
	float att = 1.0 / (1.0 + 0.1*d + 0.01*d*d);
	float lambert = max(dot(N, L), 0.0);
	float spec = pow(max(dot(N, H), 0.0), shininess);
	
	vec3 diffuse  = (Ka + att * Kd * lambert) * vec3(texture2D(texsamp, vtex));
	vec3 specular = att * Ks * spec * vec3(texture2D(specsamp, vtex));
	
	gl_FragColor = vec4(diffuse + specular, 1.0);
}