package com.example.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import java.awt.Color;

public class DrawHelper {

    // ══════════════════════════════════════════════════════════════
    // SHADER — компилируем вручную через LWJGL
    // ══════════════════════════════════════════════════════════════

    private static int            programId     = -1;
    private static ShaderProgram  shaderWrapper = null;

    // ── Vertex Shader ─────────────────────────────────────────────
    private static final String VSH =
        "#version 150\n" +
        "in vec3 Position;\n" +
        "in vec2 UV0;\n" +
        "uniform mat4 ModelViewMat;\n" +
        "uniform mat4 ProjMat;\n" +
        "out vec2 texCoord;\n" +
        "void main() {\n" +
        "    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);\n" +
        "    texCoord = UV0;\n" +
        "}\n";

    // ── Fragment Shader ───────────────────────────────────────────
    private static final String FSH =
        "#version 150\n" +
        "in vec2 texCoord;\n" +
        "out vec4 fragColor;\n" +
        "uniform vec2  u_Resolution;\n" +
        "uniform vec4  u_Rect;\n" +
        "uniform float u_Radius;\n" +
        "uniform vec4  u_Color;\n" +
        "uniform vec4  u_OutlineColor;\n" +
        "uniform float u_OutlineWidth;\n" +
        "uniform vec4  u_GlowColor;\n" +
        "uniform float u_GlowRadius;\n" +
        "uniform float u_Time;\n" +
        "float roundedBoxSDF(vec2 p, vec2 b, float r) {\n" +
        "    vec2 q = abs(p) - b + r;\n" +
        "    return length(max(q,0.0)) + min(max(q.x,q.y),0.0) - r;\n" +
        "}\n" +
        "vec3 hsv2rgb(vec3 c) {\n" +
        "    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);\n" +
        "    vec3 p = abs(fract(c.xxx + K.xyz)*6.0 - K.www);\n" +
        "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
        "}\n" +
        "void main() {\n" +
        "    vec2 fragPos  = u_Rect.xy + texCoord * u_Rect.zw;\n" +
        "    vec2 center   = u_Rect.xy + u_Rect.zw * 0.5;\n" +
        "    vec2 halfSize = u_Rect.zw * 0.5;\n" +
        "    vec2 p        = fragPos - center;\n" +
        "    float dist = roundedBoxSDF(p, halfSize, u_Radius);\n" +
        "    float aa   = 0.8;\n" +
        "    float glowAlpha = 0.0;\n" +
        "    if (u_GlowRadius > 0.0 && u_GlowColor.a > 0.0) {\n" +
        "        float g = clamp(dist / u_GlowRadius, 0.0, 1.0);\n" +
        "        glowAlpha = (1.0 - g*g) * u_GlowColor.a;\n" +
        "    }\n" +
        "    float outlineAlpha = 0.0;\n" +
        "    if (u_OutlineWidth > 0.0 && u_OutlineColor.a > 0.0) {\n" +
        "        float outerDist = dist + u_OutlineWidth;\n" +
        "        outlineAlpha = smoothstep(aa,-aa,outerDist) - smoothstep(aa,-aa,dist);\n" +
        "        outlineAlpha *= u_OutlineColor.a;\n" +
        "    }\n" +
        "    float fillAlpha = smoothstep(aa,-aa,dist) * u_Color.a;\n" +
        "    vec3 fillRGB = u_Color.rgb;\n" +
        "    if (u_Time > 0.0 && fillAlpha > 0.0) {\n" +
        "        float normX = (p.x + halfSize.x) / u_Rect.z;\n" +
        "        float hue   = fract(normX * 0.45 + u_Time * 0.13);\n" +
        "        vec3 shimmer = hsv2rgb(vec3(hue, 0.50, 1.0));\n" +
        "        fillRGB = mix(fillRGB, shimmer, 0.30);\n" +
        "    }\n" +
        "    vec3 col = vec3(0.0); float a = 0.0;\n" +
        "    col = mix(col, u_GlowColor.rgb, glowAlpha);    a = max(a, glowAlpha);\n" +
        "    col = mix(col, u_OutlineColor.rgb, outlineAlpha); a = max(a, outlineAlpha);\n" +
        "    col = mix(col, fillRGB, fillAlpha);            a = max(a, fillAlpha);\n" +
        "    if (a < 0.004) discard;\n" +
        "    fragColor = vec4(col, a);\n" +
        "}\n";

    /**
     * Вызывается из ResourceReloadListener.
     * ResourceFactory не нужен — шейдер встроен в строки выше.
     */
    public static void init(ResourceFactory rm) {
        // Удаляем старый шейдер если есть
        if (programId != -1) {
            GL20.glDeleteProgram(programId);
            programId     = -1;
            shaderWrapper = null;
        }

        try {
            int vert = compileShader(GL20.GL_VERTEX_SHADER,   VSH);
            int frag = compileShader(GL20.GL_FRAGMENT_SHADER, FSH);

            programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, vert);
            GL20.glAttachShader(programId, frag);

            // Обязательно до linkProgram!
            GL20.glBindAttribLocation(programId, 0, "Position");
            GL20.glBindAttribLocation(programId, 1, "UV0");

            GL20.glLinkProgram(programId);

            int status = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
            if (status == GL20.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(programId);
                System.err.println("[DrawHelper] Link FAILED: " + log);
                GL20.glDeleteProgram(programId);
                programId = -1;
            } else {
                // ✅ new ShaderProgram(int) — единственный конструктор в 1.21.4
                shaderWrapper = new ShaderProgram(programId);
                System.out.println("[DrawHelper] Shader OK, id=" + programId);
            }

            GL20.glDeleteShader(vert);
            GL20.glDeleteShader(frag);

        } catch (Exception e) {
            System.err.println("[DrawHelper] init FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int compileShader(int type, String src) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            System.err.println("[DrawHelper] Compile error:\n" + GL20.glGetShaderInfoLog(id));
        }
        return id;
    }

    private static boolean ok() { return programId != -1 && shaderWrapper != null; }

    // ══════════════════════════════════════════════════════════════
    // ПУБЛИЧНЫЕ МЕТОДЫ РИСОВАНИЯ
    // ══════════════════════════════════════════════════════════════

    public static void drawRect(Matrix4f m, float x, float y, float w, float h,
                                float radius, Color color) {
        sdf(m, x, y, w, h, radius, color, null, 0, null, 0, -1);
    }

    public static void drawRect(Matrix4f m, float x, float y, float w, float h, Color color) {
        sdf(m, x, y, w, h, 0, color, null, 0, null, 0, -1);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color color) {
        sdf(ctx.getMatrices().peek().getPositionMatrix(),
            x, y, w, h, 0, color, null, 0, null, 0, -1);
    }

    public static void drawStyledRect(Matrix4f m, float x, float y, float w, float h,
                                      float tl, float tr, float br, float bl, Color color) {
        if (tl == tr && tr == br && br == bl) {
            drawRect(m, x, y, w, h, tl, color);
            return;
        }
        float topR = Math.max(tl, tr);
        float botR = Math.max(bl, br);
        float half = h / 2f;

        sdf(m, x, y,        w, half + 1, topR, color, null, 0, null, 0, -1);
        sdf(m, x, y + half, w, half + 1, botR, color, null, 0, null, 0, -1);

        if (tl == 0 && topR > 0) drawRectRaw(m, x,            y,            topR, topR, color);
        if (tr == 0 && topR > 0) drawRectRaw(m, x + w - topR, y,            topR, topR, color);
        if (bl == 0 && botR > 0) drawRectRaw(m, x,            y + h - botR, botR, botR, color);
        if (br == 0 && botR > 0) drawRectRaw(m, x + w - botR, y + h - botR, botR, botR, color);
    }

    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h,
                                   float radius, Color color, float thickness) {
        sdf(ms.peek().getPositionMatrix(),
            x - thickness, y - thickness,
            w + thickness * 2, h + thickness * 2,
            radius + thickness,
            new Color(0, 0, 0, 0),
            color, thickness, null, 0, -1);
    }

    public static void drawGlow(Matrix4f m, float x, float y, float w, float h,
                                float radius, float spread, Color color) {
        sdf(m, x - spread, y - spread,
            w + spread * 2f, h + spread * 2f,
            radius + spread,
            new Color(0, 0, 0, 0), null, 0,
            color, spread, -1);
    }

    public static void drawShimmer(Matrix4f m, float x, float y, float w, float h,
                                   float radius, Color baseColor) {
        float time = (System.currentTimeMillis() % 100_000L) / 1000f;
        sdf(m, x, y, w, h, radius, baseColor, null, 0, null, 0, time);
    }

    public static void drawCircle(Matrix4f m, float cx, float cy, int segments,
                                  float r, Color color) {
        sdf(m, cx - r, cy - r, r * 2, r * 2, r, color, null, 0, null, 0, -1);
    }

    // ══════════════════════════════════════════════════════════════
    // RAINBOW
    // ══════════════════════════════════════════════════════════════

    public static Color getRainbow(float offset, float speed, float sat,
                                   float brightness, int alpha) {
        float hue = ((System.currentTimeMillis() % (long)(speed * 1000))
                     / (speed * 1000f) + offset) % 1f;
        Color c = Color.getHSBColor(hue, sat, brightness);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // ══════════════════════════════════════════════════════════════
    // ГРАДИЕНТЫ
    // ══════════════════════════════════════════════════════════════

    public static void drawGradientV(DrawContext ctx, float x, float y,
                                     float w, float h, Color top, Color bottom) {
        ctx.fillGradient((int)x, (int)y, (int)(x+w), (int)(y+h),
                         top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientH(Matrix4f m, float x, float y, float w, float h,
                                     Color left, Color right) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,   y+h, 0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x+w, y+h, 0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x+w, y,   0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x,   y,   0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════
    // ТЕКСТ
    // ══════════════════════════════════════════════════════════════

    public static void drawText(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.medium(text), (int)x, (int)y, c.getRGB(), false);
    }

    public static void drawTextBold(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.bold(text), (int)x, (int)y, c.getRGB(), false);
    }

    public static void drawTextShadow(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.medium(text), (int)x, (int)y, c.getRGB(), true);
    }

    public static void drawTextBoldShadow(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.bold(text), (int)x, (int)y, c.getRGB(), true);
    }

    public static void drawTextRegular(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.regular(text), (int)x, (int)y, c.getRGB(), false);
    }

    // ══════════════════════════════════════════════════════════════
    // RAW RECT (без шейдера)
    // ══════════════════════════════════════════════════════════════

    public static void drawRectRaw(Matrix4f m, float x, float y, float w, float h, Color c) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float r = c.getRed()/255f, g = c.getGreen()/255f,
              b = c.getBlue()/255f, a = c.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,   y+h, 0).color(r, g, b, a);
        buf.vertex(m, x+w, y+h, 0).color(r, g, b, a);
        buf.vertex(m, x+w, y,   0).color(r, g, b, a);
        buf.vertex(m, x,   y,   0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════
    // CORE SDF
    // ══════════════════════════════════════════════════════════════

    private static void sdf(Matrix4f mat,
                            float x, float y, float w, float h,
                            float radius,
                            Color fill,
                            Color outlineColor, float outlineWidth,
                            Color glowColor,    float glowRadius,
                            float time) {
        if (!ok() || w <= 0 || h <= 0) return;

        Color fc = fill         != null ? fill         : new Color(0, 0, 0, 0);
        Color oc = outlineColor != null ? outlineColor : new Color(0, 0, 0, 0);
        Color gc = glowColor    != null ? glowColor    : new Color(0, 0, 0, 0);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        // ✅ Передаём ShaderProgram напрямую — работает в 1.21.4
        RenderSystem.setShader(shaderWrapper);

        // ✅ Активируем программу и ставим uniforms через GL20
        GL20.glUseProgram(programId);

        u2f("u_Resolution", w, h);
        u4f("u_Rect",       0, 0, w, h);
        u1f("u_Radius",     Math.min(radius, Math.min(w, h) / 2f));
        u4f("u_Color",
            fc.getRed()/255f, fc.getGreen()/255f, fc.getBlue()/255f, fc.getAlpha()/255f);
        u4f("u_OutlineColor",
            oc.getRed()/255f, oc.getGreen()/255f, oc.getBlue()/255f, oc.getAlpha()/255f);
        u1f("u_OutlineWidth", outlineColor != null ? outlineWidth : 0f);
        u4f("u_GlowColor",
            gc.getRed()/255f, gc.getGreen()/255f, gc.getBlue()/255f, gc.getAlpha()/255f);
        u1f("u_GlowRadius", glowColor != null ? glowRadius : 0f);
        u1f("u_Time",       time < 0 ? 0f : time);

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(mat, x,   y,   0).texture(0, 0);
        buf.vertex(mat, x,   y+h, 0).texture(0, 1);
        buf.vertex(mat, x+w, y+h, 0).texture(1, 1);
        buf.vertex(mat, x+w, y,   0).texture(1, 0);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════
    // GL20 UNIFORM HELPERS
    // ══════════════════════════════════════════════════════════════

    private static void u1f(String n, float a) {
        int l = GL20.glGetUniformLocation(programId, n);
        if (l >= 0) GL20.glUniform1f(l, a);
    }

    private static void u2f(String n, float a, float b) {
        int l = GL20.glGetUniformLocation(programId, n);
        if (l >= 0) GL20.glUniform2f(l, a, b);
    }

    private static void u4f(String n, float a, float b, float c, float d) {
        int l = GL20.glGetUniformLocation(programId, n);
        if (l >= 0) GL20.glUniform4f(l, a, b, c, d);
    }
}
