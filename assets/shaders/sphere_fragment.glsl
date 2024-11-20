#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;  // The world map texture (rectangular)
uniform float u_time;         // Time variable for rotation (controls how much the globe spins)
uniform vec3 u_lightDir;      // Direction of the light (for shading)

varying vec2 v_texCoords;     // Texture coordinates from the vertex shader

void main() {
    // Sphere radius
    float radius = 0.5;

    //float u = 0.5 + atan(y, x) / (2.0 * PI);  // Horizontal wrapping (longitude)
    //float v = 0.5 - asin(z) / PI;  // Vertical wrapping (latitude)

    // Offset texture coordinates so (0.5, 0.5) is the center of the sphere
    vec2 uv = v_texCoords - vec2(0.5, 0.5);

    // Calculate the distance from the center of the sphere
    float dist = length(uv);

    // Discard fragments outside the sphere (makes the shape circular)
    if (dist > radius) {
        discard;
    }

    // Map from the 2D plane to spherical coordinates (latitude and longitude)
    // Latitude = vertical angle, Longitude = horizontal angle
    float theta = asin(uv.y / radius);  // Latitude (-π/2 to π/2)
    float phi = atan(uv.x, sqrt(radius * radius - uv.x * uv.x - uv.y * uv.y));  // Longitude (-π to π)

    // Apply rotation to the globe around the vertical axis (y-axis)
    float rotationSpeed = 0.5;  // Adjust rotation speed
    float rotatedPhi = phi + u_time * rotationSpeed;  // Rotate longitude (phi) over time

    // Convert spherical coordinates (theta, rotatedPhi) back to UV space for the 2D map texture
    vec2 texCoords;
    texCoords.x = (rotatedPhi / (2.0 * 3.14159265359)) + 0.5;  // Map [-π, π] to [0, 1] for texture lookup
    texCoords.y = (theta / 3.14159265359) + 0.5;               // Map [-π/2, π/2] to [0, 1]

    // Fetch the color from the world map texture
    vec4 color = texture2D(u_texture, texCoords);

    // Basic lighting using a simple Lambertian model (optional)
    vec3 normal;
    normal.xy = uv / radius;                       // Normalized x and y coordinates for the sphere
    normal.z = sqrt(1.0 - dot(normal.xy, normal.xy));  // Calculate z-component of the normal
    float lightIntensity = max(dot(normal, u_lightDir), 0);  // Lambertian lighting

    // Apply lighting to the texture color
    vec4 finalColor = color * lightIntensity;

    // Ensure the final color is not too dark
    finalColor.rgb = mix(vec3(0.1, 0.1, 0.1), finalColor.rgb, lightIntensity);
    finalColor.a = 1;

    // Output the final color
    gl_FragColor = finalColor;
}
