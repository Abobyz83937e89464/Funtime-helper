package com.example.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.Color;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class DrawHelper {

    // ═══════════════════════════════════════════════════════
    // SHADER — чистый GL20, без Minecraft ShaderProgram API
    // ═══════════════════════════════════════════════════════

    private static int  prog = -1;
    private static boolean dead = false;

    public static void initShader() {
        if (prog != -1 || dead) return;
        try {
            int vs = compileShader(GL20.GL_VERTEX_SHADER,
                readRes("/assets/nocturn-client/shaders/core/rounded_rect.vsh"));
            int fs = compileShader(GL20.GL_FRAGMENT_SHADER,
                readRes("/assets/nocturn-client/shaders/core/rounded_rect.fsh"));

            prog = GL20.glCreateProgram();
            GL20.glAttachShader(prog, vs);
            GL20.glAttachShader(prog, fs);
            GL20.glBindAttribLocation(prog, 0, "Position");
            GL20.glBindAttribLocation(prog, 1, "UV0");
            GL20.glLinkProgram(prog);

            if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                System.err.println("[DrawHelper] Link error: "
                    + GL20.glGetProgramInfoLog(prog));
                dead = true; prog = -1; return;
            }
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            System.out.println("[DrawHelper] Shader OK, id=" + prog);

        } catch (Exception e) {
            System.err.println("[DrawHelper] initShader failed: " + e.getMessage());
            dead = true;
        }
    }

    private static int compileShader(int type, String src) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
            System.err.println("[DrawHelper] Shader compile error:\n"
                + GL20.glGetShaderInfoLog(id));
        return id;
    }

    private static String readRes(String path) throws Exception {
        try (InputStream is = Objects.requireNonNull(
                DrawHelper.class.getResourceAsStream(path),
                "Not found: " + path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static boolean ok() { return prog != -1; }

    // ── Uniform helpers ───────────────────────────────────────────────────

    private static void u1f(String n, float a) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) GL20.glUniform1f(l, a);
    }

    private static void u2f(String n, float a, float b) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) GL20.glUniform2f(l, a, b);
    }

    private static void u4f(String n, float a, float b, float c, float d) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) GL20.glUniform4f(l, a, b, c, d);
    }

    private static void uMat4(String n, Matrix4f mat) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) {
            float[] buf = new float[16];
            mat.get(buf);
            GL20.glUniformMatrix4fv(l, false, buf);
        }
    }

    // ═══════════════════════════════════════════════════════
    // CORE SDF
    // ═══════════════════════════════════════════════════════

    private static void sdf(Matrix4f mat,
                             float x, float y, float w, float h,
                             float radius,
                             Color fill,
                             Color outlineColor, float outlineWidth,
                             Color glowColor,    float glowRadius,
                             float time) {
        if (w <= 0 || h <= 0) return;

        if (!ok()) {
            if (fill != null && fill.getAlpha() > 0)
                drawRectRaw(mat, x, y, w, h, fill);
            return;
        }

        Color fc = fill         != null ? fill         : new Color(0,0,0,0);
        Color oc = outlineColor != null ? outlineColor : new Color(0,0,0,0);
        Color gc = glowColor    != null ? glowColor    : new Color(0,0,0,0);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFuncSeparate(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,       GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL20.glUseProgram(prog);

        uMat4("ModelViewMat", RenderSystem.getModelViewMatrix());
        uMat4("ProjMat",      RenderSystem.getProjectionMatrix());
        u2f("u_Resolution", w, h);
        u4f("u_Rect",       0, 0, w, h);
        u1f("u_Radius",     Math.min(radius, Math.min(w, h) / 2f));
        u4f("u_Color",
            fc.getRed()/255f, fc.getGreen()/255f,
            fc.getBlue()/255f, fc.getAlpha()/255f);
        u4f("u_OutlineColor",
            oc.getRed()/255f, oc.getGreen()/255f,
            oc.getBlue()/255f, oc.getAlpha()/255f);
        u1f("u_OutlineWidth", outlineColor != null ? outlineWidth : 0f);
        u4f("u_GlowColor",
            gc.getRed()/255f, gc.getGreen()/255f,
            gc.getBlue()/255f, gc.getAlpha()/255f);
        u1f("u_GlowRadius", glowColor != null ? glowRadius : 0f);
        u1f("u_Time",       time < 0f ? 0f : time);

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(mat, x,     y,     0).texture(0f, 0f);
        buf.vertex(mat, x,     y + h, 0).texture(0f, 1f);
        buf.vertex(mat, x + w, y + h, 0).texture(1f, 1f);
        buf.vertex(mat, x + w, y,     0).texture(1f, 0f);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        GL20.glUseProgram(0);
        GL11.glDisable(GL11.GL_BLEND);
    }

    // ═══════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════

    public static void drawRect(Matrix4f m, float x, float y,
                                float w, float h,
                                float radius, Color color) {
        sdf(m, x, y, w, h, radius, color, null, 0f, null, 0f, -1f);
    }

    public static void drawRect(Matrix4f m, float x, float y,
                                float w, float h, Color color) {
        sdf(m, x, y, w, h, 0f, color, null, 0f, null, 0f, -1f);
    }

    public static void drawRect(DrawContext ctx, float x, float y,
                                float w, float h, Color color) {
        drawRect(ctx.getMatrices().peek().getPositionMatrix(),
                 x, y, w, h, 0f, color);
    }

    public static void drawStyledRect(Matrix4f m,
                                      float x, float y, float w, float h,
                                      float tl, float tr,
                                      float br, float bl,
                                      Color color) {
        if (tl == tr && tr == br && br == bl) {
            drawRect(m, x, y, w, h, tl, color);
            return;
        }
        float topR = Math.max(tl, tr);
        float botR = Math.max(bl, br);
        float half = h / 2f;

        sdf(m, x, y,        w, half + 1f, topR, color, null, 0f, null, 0f, -1f);
        sdf(m, x, y + half, w, half + 1f, botR, color, null, 0f, null, 0f, -1f);

        if (tl == 0 && topR > 0) drawRectRaw(m, x,            y,            topR, topR, color);
        if (tr == 0 && topR > 0) drawRectRaw(m, x + w - topR, y,            topR, topR, color);
        if (bl == 0 && botR > 0) drawRectRaw(m, x,            y + h - botR, botR, botR, color);
        if (br == 0 && botR > 0) drawRectRaw(m, x + w - botR, y + h - botR, botR, botR, color);
    }

    public static void drawOutline(MatrixStack ms,
                                   float x, float y, float w, float h,
                                   float radius, Color color, float thickness) {
        sdf(ms.peek().getPositionMatrix(),
            x - thickness, y - thickness,
            w + thickness * 2f, h + thickness * 2f,
            radius + thickness,
            new Color(0, 0, 0, 0),
            color, thickness,
            null, 0f, -1f);
    }

    public static void drawGlow(Matrix4f m,
                                float x, float y, float w, float h,
                                float radius, float spread, Color color) {
        sdf(m,
            x - spread, y - spread,
            w + spread * 2f, h + spread * 2f,
            radius + spread,
            new Color(0, 0, 0, 0),
            null, 0f,
            color, spread, -1f);
    }

    public static void drawShimmer(Matrix4f m,
                                   float x, float y, float w, float h,
                                   float radius, Color baseColor) {
        float time = (System.currentTimeMillis() % 100_000L) / 1000f;
        sdf(m, x, y, w, h, radius, baseColor, null, 0f, null, 0f, time);
    }

    public static void drawCircle(Matrix4f m,
                                  float cx, float cy,
                                  int segments, float r, Color color) {
        sdf(m, cx - r, cy - r, r * 2f, r * 2f, r,
            color, null, 0f, null, 0f, -1f);
    }

    // ═══════════════════════════════════════════════════════
    // RAINBOW
    // ═══════════════════════════════════════════════════════

    public static Color getRainbow(float offset, float speed,
                                   float sat, float brightness, int alpha) {
        float hue = ((System.currentTimeMillis() % (long)(speed * 1000f))
                     / (speed * 1000f) + offset) % 1f;
        Color c = Color.getHSBColor(hue, sat, brightness);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // ═══════════════════════════════════════════════════════
    // GRADIENTS
    // ═══════════════════════════════════════════════════════

    public static void drawGradientV(DrawContext ctx,
                                     float x, float y, float w, float h,
                                     Color top, Color bottom) {
        ctx.fillGradient(
            (int) x, (int) y,
            (int)(x + w), (int)(y + h),
            top.getRGB(), bottom.getRGB()
        );
    }

    public static void drawGradientH(Matrix4f m,
                                     float x, float y, float w, float h,
                                     Color left, Color right) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0)
           .color(left.getRed(),  left.getGreen(),
                  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x + w, y + h, 0)
           .color(right.getRed(), right.getGreen(),
                  right.getBlue(), right.getAlpha());
        buf.vertex(m, x + w, y,     0)
           .color(right.getRed(), right.getGreen(),
                  right.getBlue(), right.getAlpha());
        buf.vertex(m, x,     y,     0)
           .color(left.getRed(),  left.getGreen(),
                  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        GL11.glDisable(GL11.GL_BLEND);
    }

    // ═══════════════════════════════════════════════════════
    // TEXT
    // ═══════════════════════════════════════════════════════

    public static void drawText(DrawContext ctx, String text,
                                float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.medium(text),
                     (int) x, (int) y, c.getRGB(), false);
    }

    public static void drawTextBold(DrawContext ctx, String text,
                                    float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.bold(text),
                     (int) x, (int) y, c.getRGB(), false);
    }

    public static void drawTextShadow(DrawContext ctx, String text,
                                      float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.medium(text),
                     (int) x, (int) y, c.getRGB(), true);
    }

    public static void drawTextBoldShadow(DrawContext ctx, String text,
                                          float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.bold(text),
                     (int) x, (int) y, c.getRGB(), true);
    }

    public static void drawTextRegular(DrawContext ctx, String text,
                                       float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.regular(text),
                     (int) x, (int) y, c.getRGB(), false);
    }

    // ═══════════════════════════════════════════════════════
    // RAW RECT
    // ═══════════════════════════════════════════════════════

    public static void drawRectRaw(Matrix4f m,
                                   float x, float y, float w, float h,
                                   Color c) {
        if (w <= 0 || h <= 0) return;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        float r = c.getRed()   / 255f;
        float g = c.getGreen() / 255f;
        float b = c.getBlue()  / 255f;
        float a = c.getAlpha() / 255f;

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y,     0).color(r, g, b, a);
        buf.vertex(m, x,     y,     0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        GL11.glDisable(GL11.GL_BLEND);
    }
}
