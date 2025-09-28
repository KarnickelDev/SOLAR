#ifdef GL_ES
precision mediump float;
#endif

#include "../../webgl-noise/noise3D.glsl"
#include "../blackbody.glsl"

varying vec2 v_uv;

uniform float u_temperature;
uniform float u_time;
uniform float u_pixelation;
uniform float u_zoom;

float coronaFactor(float T) {
    float shaped = pow(smoothstep(1000, 30000, T), 0.36);
    return mix(0.4, 0.6, shaped);
}

float twinkleFactor(float T) {
    float shaped = pow(smoothstep(1000, 30000, T), 0.4);
    return mix(0.6, 2.0, shaped);
}

void main() {
    vec2 uv = v_uv;

    if(u_pixelation != 0) uv = floor(uv / u_pixelation) * u_pixelation;

    float r = length(uv) * 4;
    if (r > 4) discard;

    // Base exponential falloff
    float falloff = exp(0.9-0.9*r);

    // Add geometric poster rays
    float angleGeo = atan(uv.y, uv.x);
    angleGeo = (angleGeo + 6.2831853) / 6.2831853; // 0..1

    int nRays = 10;
    float sector = floor(angleGeo * nRays);

    // Per-ray dynamic brightness
    float rayStrength = 0.6 + twinkleFactor(u_temperature) * (snoise(vec3(sector, u_time*0.5, 0.0)) + 1);

    // Ray mask (hard sunburst wedge)
    float rayMask = step(0.5 + 0.2*snoise(vec3(angleGeo*4, u_time*0.1, 1)), fract(angleGeo * nRays));

    // Final ray intensity
    float rayGeo = rayMask * rayStrength;



    float plasma = 0.2*(0.5 + 0.5*snoise(vec3(uv, u_time*0.1)));
    float noisyFalloff = falloff - plasma;



    // Add radial ray-like noise
    float angleN = atan(uv.y, uv.x);
    float rayNoise = snoise(vec3(cos(angleN)*2.5, sin(angleN)*2.0, u_time*0.3));
    float rayN = (0.5 + 0.1 * rayNoise);

    float intensity = noisyFalloff + (pow(falloff, 4) * 2 * rayN);
    intensity *= coronaFactor(u_temperature);

    // add geometric poster rays, fade in when zooming out
    intensity += rayGeo * exp(-0.5 * r) * 0.0014 * pow(u_zoom, 0.5);

    // Temperature-driven color (blackbody to bluish-white)
    vec3 bb = blackBody(u_temperature);
    vec3 coronaColor = bb*1.1;

    gl_FragColor = vec4(coronaColor * intensity, intensity);
}
