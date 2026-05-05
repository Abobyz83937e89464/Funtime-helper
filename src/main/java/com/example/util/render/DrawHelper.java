package com.example.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    // ═══════════════════════════════════════════════════════════════
    // SHADER REF  (устанавливается через CoreShaderRegistrationCallback в NocturnClient)
    // ═══════════════════════════════════════════════════════════════

    public static ShaderProgram shader; // public — чтобы NocturnClient мог записать

    private static boolean ok() { return shader != null; }

    // ═══════════════════════════════════════════════════════════════
    // ОСНОВНЫЕ МЕТОДЫ — сигнатуры не изменились
    // ═══════════════════════════════════════════════════════════════

    /** Скруглённый прямоугольник */
    public static void drawRect(Matrix4f m, float x, float y, float w, float h,
                                float radius, Color color) {
        sdf(m, x, y, w, h, radius, color, null, 0, null, 0, -1);
    }

    /** Без радиуса */
    public static void drawRect(Matrix4f m, float x, float y, float w, float h, Color color) {
        sdf(m, x, y, w, h, 0, color, null, 0, null, 0, -1);
    }

    /** Через DrawContext */
    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color color) {
        sdf(ctx.getMatrices().peek().getPositionMatrix(),
            x, y, w, h, 0, color, null, 0, null, 0, -1);
    }

    /**
     * Разные радиусы по углам: tl=верх-лево, tr=верх-право, br=низ-право, bl=низ-лево
     */
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

    /** Outline (обводка) */
    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h,
                                   float radius, Color color, float thickness) {
        sdf(ms.peek().getPositionMatrix(),
            x - thickness, y - thickness,
            w + thickness * 2, h + thickness * 2,
            radius + thickness,
            new Color(0, 0, 0, 0),
            color, thickness,
            null, 0, -1);
    }

    /** Мягкое свечение вокруг прямоугольника */
    public static void drawGlow(Matrix4f m, float x, float y, float w, float h,
                                float radius, int spread, Color color) {
        sdf(m,
            x - spread, y - spread,
            w + spread * 2f, h + spread * 2f,
            radius + spread,
            new Color(0, 0, 0, 0),
            null, 0,
            color, spread, -1);
    }

    /** Переливающийся прямоугольник (shimmer) */
    public static void drawShimmer(Matrix4f m, float x, float y, float w, float h,
                                   float radius, Color baseColor) {
        float time = (System.currentTimeMillis() % 100_000L) / 1000f;
        sdf(m, x, y, w, h, radius, baseColor, null, 0, null, 0, time);
    }

    /** Круг через SDF */
    public static void drawCircle(Matrix4f m, float cx, float cy, int segments, float r, Color color) {
        sdf(m, cx - r, cy - r, r * 2, r * 2, r, color, null, 0, null, 0, -1);
    }

    // ═══════════════════════════════════════════════════════════════
    // RAINBOW
    // ═══════════════════════════════════════════════════════════════

    public static Color getRainbow(float offset, float speed, float sat, float brightness, int alpha) {
        float hue = ((System.currentTimeMillis() % (long)(speed * 1000)) / (speed * 1000f) + offset) % 1f;
        Color c = Color.getHSBColor(hue, sat, brightness);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // ═══════════════════════════════════════════════════════════════
    // ГРАДИЕНТЫ
    // ═══════════════════════════════════════════════════════════════

    public static void drawGradientV(DrawContext ctx, float x, float y, float w, float h,
                                     Color top, Color bottom) {
        ctx.fillGradient((int)x, (int)y, (int)(x + w), (int)(y + h),
                         top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientH(Matrix4f m, float x, float y, float w, float h,
                                     Color left, Color right) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x + w, y + h, 0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x + w, y,     0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x,     y,     0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ═══════════════════════════════════════════════════════════════
    // ТЕКСТ
    // ═══════════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════════
    // RAW RECT  (для разделителей 1px — шейдер не нужен)
    // ═══════════════════════════════════════════════════════════════

    public static void drawRectRaw(Matrix4f m, float x, float y, float w, float h, Color c) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float r = c.getRed() / 255f, g = c.getGreen() / 255f,
              b = c.getBlue() / 255f, a = c.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y,     0).color(r, g, b, a);
        buf.vertex(m, x,     y,     0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ═══════════════════════════════════════════════════════════════
    // CORE SDF DRAW
    // ═══════════════════════════════════════════════════════════════

    private static void sdf(Matrix4f mat,
                            float x,    float y,    float w,    float h,
                            float radius,
                            Color fill,
                            Color outlineColor, float outlineWidth,
                            Color glowColor,    float glowRadius,
                            float time) {
        if (!ok() || w <= 0 || h <= 0) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        // ─── 1. Активируем шейдер ───────────────────────────────────────
        // В 1.21.4 принимает Supplier<ShaderProgram> — лямбда с нашей ссылкой
        RenderSystem.setShader(() -> shader);

        // ─── 2. Uniforms через GlUniform (НЕ через GL20 напрямую) ────────
        // u_Resolution  — размер quad'а в пикселях (шейдер считает SDF в этом пространстве)
        setUniform2f("u_Resolution", w, h);
        // u_Rect        — (originX, originY, width, height); origin=0,0 т.к. матрица уже сдвинута
        setUniform4f("u_Rect",      0, 0, w, h);
        setUniform1f("u_Radius",    Math.min(radius, Math.min(w, h) / 2f));

        setUniformColor("u_Color",        fill         != null ? fill         : new Color(0, 0, 0, 0));
        setUniformColor("u_OutlineColor", outlineColor != null ? outlineColor : new Color(0, 0, 0, 0));
        setUniform1f("u_OutlineWidth",    outlineColor != null ? outlineWidth : 0f);
        setUniformColor("u_GlowColor",    glowColor    != null ? glowColor    : new Color(0, 0, 0, 0));
        setUniform1f("u_GlowRadius",      glowColor    != null ? glowRadius   : 0f);
        setUniform1f("u_Time",            time < 0 ? 0f : time);

        // ─── 3. Геометрия (quad с UV 0-1) ───────────────────────────────
        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(mat, x,     y,     0).texture(0, 0);
        buf.vertex(mat, x,     y + h, 0).texture(0, 1);
        buf.vertex(mat, x + w, y + h, 0).texture(1, 1);
        buf.vertex(mat, x + w, y,     0).texture(1, 0);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.disableBlend();
    }

    // ═══════════════════════════════════════════════════════════════
    // UNIFORM HELPERS  — используют GlUniform, не GL20 напрямую
    // ═══════════════════════════════════════════════════════════════

    private static void setUniform1f(String name, float a) {
        GlUniform u = shader.getUniform(name);
        if (u != null) u.set(a);
    }

    private static void setUniform2f(String name, float a, float b) {
        GlUniform u = shader.getUniform(name);
        if (u != null) u.set(a, b);
    }

    private static void setUniform4f(String name, float a, float b, float c, float d) {
        GlUniform u = shader.getUniform(name);
        if (u != null) u.set(a, b, c, d);
    }

    private static void setUniformColor(String name, Color c) {
        setUniform4f(name,
            c.getRed()   / 255f,
            c.getGreen() / 255f,
            c.getBlue()  / 255f,
            c.getAlpha() / 255f);
    }
                                             }
