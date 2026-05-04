#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform vec2  u_Resolution;
uniform vec4  u_Rect;
uniform float u_Radius;
uniform vec4  u_Color;
uniform vec4  u_OutlineColor;
uniform float u_OutlineWidth;
uniform vec4  u_GlowColor;
uniform float u_GlowRadius;
uniform float u_Time;

float roundedBoxSDF(vec2 p, vec2 halfSize, float r) {
    vec2 q = abs(p) - halfSize + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 fragPos  = u_Rect.xy + texCoord * u_Rect.zw;
    vec2 center   = u_Rect.xy + u_Rect.zw * 0.5;
    vec2 halfSize = u_Rect.zw * 0.5;
    vec2 p        = fragPos - center;

    float dist = roundedBoxSDF(p, halfSize, u_Radius);
    float aa   = 0.8;

    // Glow
    float glowAlpha = 0.0;
    if (u_GlowRadius > 0.0 && u_GlowColor.a > 0.0) {
        float g = clamp(dist / u_GlowRadius, 0.0, 1.0);
        glowAlpha = (1.0 - g * g) * u_GlowColor.a;
    }

    // Outline
    float outlineAlpha = 0.0;
    if (u_OutlineWidth > 0.0 && u_OutlineColor.a > 0.0) {
        float outerDist = dist + u_OutlineWidth;
        outlineAlpha = smoothstep(aa, -aa, outerDist)
                     - smoothstep(aa, -aa, dist);
        outlineAlpha *= u_OutlineColor.a;
    }

    // Fill
    float fillAlpha = smoothstep(aa, -aa, dist) * u_Color.a;

    vec3 fillRGB = u_Color.rgb;
    if (u_Time > 0.0 && fillAlpha > 0.0) {
        float normX = (p.x + halfSize.x) / u_Rect.z;
        float hue   = fract(normX * 0.45 + u_Time * 0.13);
        vec3  shimmer = hsv2rgb(vec3(hue, 0.50, 1.0));
        fillRGB = mix(fillRGB, shimmer, 0.30);
    }

    // Compose
    vec3  col = vec3(0.0);
    float a   = 0.0;

    col = mix(col, u_GlowColor.rgb, glowAlpha);
    a   = max(a, glowAlpha);

    col = mix(col, u_OutlineColor.rgb, outlineAlpha);
    a   = max(a, outlineAlpha);

    col = mix(col, fillRGB, fillAlpha);
    a   = max(a, fillAlpha);

    if (a < 0.004) discard;
    fragColor = vec4(col, a);
}
