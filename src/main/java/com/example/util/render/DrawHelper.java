package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    // ══════════════════════════════════════════════════════════════════════
    // FILLED RECTANGLES
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Скруглённый прямоугольник (все углы одинаковые).
     */
    public static void drawRect(Matrix4f m, float x, float y, float w, float h, float r, Color color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w / 2f, h / 2f));
        // Центральная горизонтальная полоса
        drawRectRaw(m, x + r, y,     w - r * 2, h,         color);
        // Левая и правая вертикальные полосы (без угловых квадратов)
        drawRectRaw(m, x,     y + r, r,         h - r * 2, color);
        drawRectRaw(m, x + w - r, y + r, r,     h - r * 2, color);
        // Четыре скруглённых угла
        drawCorner(m, x + r,     y + r,     r, 180, color);
        drawCorner(m, x + w - r, y + r,     r, 270, color);
        drawCorner(m, x + w - r, y + h - r, r, 0,   color);
        drawCorner(m, x + r,     y + h - r, r, 90,  color);
    }

    /**
     * Простой прямоугольник без скруглений через DrawContext.
     * Используй для полноэкранного оверлея и подобного.
     */
    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color color) {
        if (w <= 0 || h <= 0) return;
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color.getRGB());
    }

    /**
     * Прямоугольник с РАЗНЫМИ радиусами для каждого угла: tl, tr, br, bl.
     * Правильная реализация — без артефактов при нулевых углах.
     */
    public static void drawStyledRect(Matrix4f m, float x, float y, float w, float h,
                                      float tl, float tr, float br, float bl, Color color) {
        float maxTop = Math.max(tl, tr);
        float maxBot = Math.max(bl, br);

        // Верхняя полоса (между TL и TR скруглениями)
        if (maxTop > 0) {
            drawRectRaw(m, x + tl, y, w - tl - tr, maxTop, color);
            // Доп. секции если один угол нулевой
            if (tl == 0 && maxTop > 0) drawRectRaw(m, x, y, tl == 0 ? 0 : tl, maxTop, color); // no-op
            if (tl < maxTop && tl > 0) drawRectRaw(m, x, y + tl, tl, maxTop - tl, color);
            if (tr < maxTop && tr > 0) drawRectRaw(m, x + w - tr, y + tr, tr, maxTop - tr, color);
        }

        // Средняя полоса (полная ширина)
        float midH = h - maxTop - maxBot;
        if (midH > 0) drawRectRaw(m, x, y + maxTop, w, midH, color);

        // Нижняя полоса (между BL и BR скруглениями)
        if (maxBot > 0) {
            drawRectRaw(m, x + bl, y + h - maxBot, w - bl - br, maxBot, color);
            if (bl < maxBot && bl > 0) drawRectRaw(m, x, y + h - maxBot, bl, maxBot - bl, color);
            if (br < maxBot && br > 0) drawRectRaw(m, x + w - br, y + h - maxBot, br, maxBot - br, color);
        }

        // Скруглённые углы
        if (tl > 0) drawCorner(m, x + tl,     y + tl,     tl, 180, color);
        if (tr > 0) drawCorner(m, x + w - tr, y + tr,     tr, 270, color);
        if (br > 0) drawCorner(m, x + w - br, y + h - br, br, 0,   color);
        if (bl > 0) drawCorner(m, x + bl,     y + h - bl, bl, 90,  color);
    }

    // ── Примитивы ─────────────────────────────────────────────────────────

    private static void drawRectRaw(Matrix4f m, float x, float y, float w, float h, Color color) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float r = color.getRed()   / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue()  / 255f;
        float a = color.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y,     0).color(r, g, b, a);
        buf.vertex(m, x,     y,     0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    /** Рисует четверть круга (pie slice) — для скруглённых углов. */
    private static void drawCorner(Matrix4f m, float cx, float cy, float r, float deg, Color color) {
        if (r <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = color.getRed()   / 255f;
        float g   = color.getGreen() / 255f;
        float b   = color.getBlue()  / 255f;
        float a   = color.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m, cx, cy, 0).color(red, g, b, a);
        for (int i = 0; i <= 16; i++) {
            double angle = Math.toRadians(deg + 90.0 * i / 16);
            buf.vertex(m, cx + (float) Math.cos(angle) * r, cy + (float) Math.sin(angle) * r, 0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════════════
    // GLOW EFFECT (без шейдеров — через многослойные полупрозрачные слои)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Рисует свечение вокруг прямоугольника.
     * @param spread   — размер свечения в пикселях
     * @param color    — цвет (alpha определяет максимальную прозрачность)
     */
    public static void drawGlow(Matrix4f m, float x, float y, float w, float h, float r, int spread, Color color) {
        for (int i = spread; i >= 1; i--) {
            float progress = (spread - i + 1f) / (spread * 1.6f);
            int alpha = (int) (color.getAlpha() * progress);
            if (alpha <= 0) continue;
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, alpha));
            float expand = (spread - i + 1);
            drawRect(m, x - expand, y - expand, w + expand * 2, h + expand * 2, r + expand, c);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // OUTLINE
    // ══════════════════════════════════════════════════════════════════════

    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h,
                                   float r, Color color, float thickness) {
        Matrix4f m = ms.peek().getPositionMatrix();
        for (float t = 0; t < thickness; t++) {
            float alpha = color.getAlpha() * (1f - t / thickness);
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) alpha);
            drawOutlineRaw(m, x - t, y - t, w + t * 2, h + t * 2, r + t, c);
        }
    }

    private static void drawOutlineRaw(Matrix4f m, float x, float y, float w, float h, float r, Color color) {
        drawRectRaw(m, x + r,     y,         w - r * 2, 1,         color);
        drawRectRaw(m, x + r,     y + h - 1, w - r * 2, 1,         color);
        drawRectRaw(m, x,         y + r,     1,         h - r * 2, color);
        drawRectRaw(m, x + w - 1, y + r,     1,         h - r * 2, color);
        drawArc(m, x + r,     y + r,     r, 180, color);
        drawArc(m, x + w - r, y + r,     r, 270, color);
        drawArc(m, x + w - r, y + h - r, r, 0,   color);
        drawArc(m, x + r,     y + h - r, r, 90,  color);
    }

    private static void drawArc(Matrix4f m, float cx, float cy, float r, float deg, Color color) {
        if (r <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = color.getRed()   / 255f;
        float g   = color.getGreen() / 255f;
        float b   = color.getBlue()  / 255f;
        float a   = color.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 16; i++) {
            double angle = Math.toRadians(deg + 90.0 * i / 16);
            float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
            buf.vertex(m, cx + cos * (r - 1), cy + sin * (r - 1), 0).color(red, g, b, a);
            buf.vertex(m, cx + cos * r,        cy + sin * r,        0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════════════
    // GRADIENTS
    // ══════════════════════════════════════════════════════════════════════

    public static void drawGradientV(DrawContext ctx, float x, float y, float w, float h,
                                     Color top, Color bottom) {
        ctx.fillGradient((int) x, (int) y, (int) (x + w), (int) (y + h),
                top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientH(Matrix4f m, float x, float y, float w, float h,
                                     Color left, Color right) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x + w, y + h, 0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x + w, y,     0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x,     y,     0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════════════
    // CIRCLE
    // ══════════════════════════════════════════════════════════════════════

    public static void drawCircle(Matrix4f m, float cx, float cy, int segments, float r, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = color.getRed()   / 255f;
        float g   = color.getGreen() / 255f;
        float b   = color.getBlue()  / 255f;
        float a   = color.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m, cx, cy, 0).color(red, g, b, a);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(360.0 * i / segments);
            buf.vertex(m, cx + (float) Math.cos(angle) * r, cy + (float) Math.sin(angle) * r, 0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEXT — кастомные Inter шрифты
    // ══════════════════════════════════════════════════════════════════════

    /** Рисует Text (с заданным шрифтом) через DrawContext */
    public static void drawText(DrawContext ctx, Text text, float x, float y, Color color, boolean shadow) {
        ctx.drawText(Fonts.get(), text, (int) x, (int) y, color.getRGB(), shadow);
    }

    /** Рисует строку шрифтом Inter Medium */
    public static void drawText(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), Fonts.medium(text), (int) x, (int) y, color.getRGB(), false);
    }

    /** Рисует строку шрифтом Inter Bold */
    public static void drawTextBold(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), Fonts.bold(text), (int) x, (int) y, color.getRGB(), false);
    }

    /** Рисует строку шрифтом Inter Medium с тенью */
    public static void drawTextShadow(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), Fonts.medium(text), (int) x, (int) y, color.getRGB(), true);
    }

    /** Рисует строку шрифтом Inter Bold с тенью */
    public static void drawTextBoldShadow(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), Fonts.bold(text), (int) x, (int) y, color.getRGB(), true);
    }

    /** Рисует строку шрифтом Inter Regular */
    public static void drawTextRegular(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), Fonts.regular(text), (int) x, (int) y, color.getRGB(), false);
    }
                     }
