#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_uv;

uniform sampler2D u_noiseTex; // optional surface noise texture
uniform vec3 u_color;          // base star color
uniform float u_edgeSoftness;  // soft edge for star outline
uniform float u_coronaIntensity; // glow strength
uniform float u_coronaRadius;   // radius multiplier for corona
uniform float u_pixelSize;       // optional pixelart effect
uniform float u_time;            // animated noise

// Hash function for generating pseudo-random gradients
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

// Interpolation (fade curve)
float fade(float t) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

// 2D Value noise
float valueNoise(vec2 uv) {
    vec2 i = floor(uv);
    vec2 f = fract(uv);

    // Corners of the cell
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    // Interpolation
    vec2 u = vec2(fade(f.x), fade(f.y));

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

// Configurable noise wrapper
float starNoise(vec2 uv, float scale, float time) {
    return valueNoise(uv * scale + vec2(time * 0.05, 0.0));
}

void main() {
    // Center UV at 0.5
    vec2 uv = v_uv - vec2(0.5);

    // Pixelart quantization
    uv = floor(uv / u_pixelSize) * u_pixelSize;

    float r = length(uv) * 2.0; // normalized radius (0=center, 1=edge)
    if (r > 1.0) discard;

    // Base outline brightness
    float outline = 1.0 - smoothstep(1.0 - u_coronaRadius - u_edgeSoftness, 1.0 - u_coronaRadius, r);

    // Corona falloff
    float corona = smoothstep(1.0, 1.0 - u_coronaRadius - u_edgeSoftness, r);
    corona = pow(corona*0.95, 1.4);

    // Surface noise
    float noiseVal = 0.7 * starNoise(uv, 333.3, 0) + 0.3 * starNoise(uv, 100.0, 0);
    noiseVal = clamp(noiseVal, 0.2, 1);

    float s = 0.2;

    // Combine outline, noise, and corona
    float brightness = outline * ((1.0-s) + s*noiseVal) + corona;

    float boost = mix(0.75, 2.5, noiseVal); // darker areas stay dark, bright areas glow
    if(outline < 1 - u_coronaRadius) {
        boost = 1f;
    }

    gl_FragColor = vec4(u_color * brightness * boost, brightness);
}
