#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform vec4  u_Rect;
uniform float u_Radius;
uniform float u_Time;
uniform float u_Toggle;     // 0.0 = выключен, 1.0 = включён
uniform float u_Hover;      // 0.0 = нет hover, 1.0 = hover
uniform vec3  u_RainbowRGB; // текущий rainbow цвет (0..1)
uniform float u_IdxOffset;  // смещение цвета для каждого модуля

// SDF скруглённого прямоугольника
float roundedBoxSDF(vec2 p, vec2 halfSize, float r) {
    vec2 q = abs(p) - halfSize + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

// HSV → RGB
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 size     = u_Rect.zw;
    vec2 halfSize = size * 0.5;
    vec2 p        = (texCoord - 0.5) * size;

    float dist = roundedBoxSDF(p, halfSize, u_Radius);
    float aa   = 0.8;

    // Базовый тёмный фон карточки
    vec3 darkBg = mix(vec3(0.11, 0.114, 0.149),
                      vec3(0.115, 0.118, 0.165),
                      u_Toggle);

    // Радужное переливание по горизонтали
    float normX  = texCoord.x;
    float hue    = fract(normX * 0.6 + u_Time * 0.09 + u_IdxOffset);
    vec3  rainbow = hsv2rgb(vec3(hue, 0.65, 1.0));

    // Градиент по вертикали — радуга сверху, темнее снизу
    float vertFade = 1.0 - texCoord.y * 0.6;

    // Финальный цвет карточки
    float shimmerStr = mix(0.0, 0.22, u_Toggle) + mix(0.0, 0.06, u_Hover);
    vec3  cardColor  = mix(darkBg, rainbow * vertFade, shimmerStr);

    // Верхняя полоска-акцент (радуга) при включении
    float lineH     = 2.0 / size.y;
    float lineAlpha = smoothstep(lineH + 0.01, lineH - 0.01, texCoord.y) * u_Toggle;
    float lineNormX = fract(texCoord.x * 0.7 + u_Time * 0.12 + u_IdxOffset);
    vec3  lineColor = hsv2rgb(vec3(lineNormX, 0.7, 1.0));
    cardColor = mix(cardColor, lineColor, lineAlpha * 0.9);

    // Hover highlight
    cardColor += vec3(0.04) * u_Hover;

    // Outline (внутренний edge)
    float outlineW  = 1.5;
    float outerDist = dist + outlineW;
    float outlineA  = (smoothstep(aa, -aa, outerDist) - smoothstep(aa, -aa, dist))
                    * mix(0.25, 0.7, u_Toggle);
    vec3  outlineC  = mix(vec3(0.13, 0.126, 0.168), rainbow, u_Toggle * 0.8 + u_Hover * 0.15);

    // Fill
    float fillAlpha = smoothstep(aa, -aa, dist);

    // Compose
    vec3  col = cardColor;
    float a   = fillAlpha;

    col = mix(col, outlineC, outlineA);
    a   = max(a, outlineA * fillAlpha);

    if (a < 0.004) discard;
    fragColor = vec4(col, a);
}
