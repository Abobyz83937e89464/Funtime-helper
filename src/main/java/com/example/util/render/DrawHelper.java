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

    /** Скруглённый прямоугольник, все углы одинаковые */
    public static void drawRect(Matrix4f m, float x, float y, float w, float h, float r, Color c) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w / 2f, h / 2f));
        if (r <= 0) { drawRectRaw(m, x, y, w, h, c); return; }

        drawRectRaw(m, x + r,     y,         w - r * 2, h,         c); // центр
        drawRectRaw(m, x,         y + r,     r,         h - r * 2, c); // лево
        drawRectRaw(m, x + w - r, y + r,     r,         h - r * 2, c); // право
        drawCorner (m, x + r,     y + r,     r, 180, c);
        drawCorner (m, x + w - r, y + r,     r, 270, c);
        drawCorner (m, x + w - r, y + h - r, r,   0, c);
        drawCorner (m, x + r,     y + h - r, r,  90, c);
    }

    /** Простой fill без скруглений через DrawContext */
    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color c) {
        if (w <= 0 || h <= 0) return;
        ctx.fill((int) x, (int) y, (int)(x + w), (int)(y + h), c.getRGB());
    }

    /**
     * Прямоугольник с РАЗНЫМИ радиусами углов: tl=верх-лево, tr=верх-право, br=низ-право, bl=низ-лево.
     * Полностью переписан — без артефактов.
     */
    public static void drawStyledRect(Matrix4f m, float x, float y, float w, float h,
                                      float tl, float tr, float br, float bl, Color c) {
        // Ограничиваем радиусы
        float maxR = Math.min(w / 2f, h / 2f);
        tl = Math.min(tl, maxR); tr = Math.min(tr, maxR);
        br = Math.min(br, maxR); bl = Math.min(bl, maxR);

        // Верхняя горизонтальная полоса (между верхними углами)
        drawRectRaw(m, x + tl, y, w - tl - tr, Math.max(tl, tr), c);
        // Средняя полоса (полная ширина, без угловых зон)
        float midTop = Math.max(tl, tr);
        float midBot = Math.max(bl, br);
        float midH   = h - midTop - midBot;
        if (midH > 0) drawRectRaw(m, x, y + midTop, w, midH, c);
        // Нижняя горизонтальная полоса
        drawRectRaw(m, x + bl, y + h - Math.max(bl, br), w - bl - br, Math.max(bl, br), c);

        // Заполняем боковые зоны у угловых секций
        if (tl < Math.max(tl, tr) && tl > 0)
            drawRectRaw(m, x, y + tl, tl, Math.max(tl, tr) - tl, c);
        if (tr < Math.max(tl, tr) && tr > 0)
            drawRectRaw(m, x + w - tr, y + tr, tr, Math.max(tl, tr) - tr, c);
        if (bl < Math.max(bl, br) && bl > 0)
            drawRectRaw(m, x, y + h - Math.max(bl, br), bl, Math.max(bl, br) - bl, c);
        if (br < Math.max(bl, br) && br > 0)
            drawRectRaw(m, x + w - br, y + h - Math.max(bl, br), br, Math.max(bl, br) - br, c);

        // Углы
        if (tl > 0) drawCorner(m, x + tl,     y + tl,     tl, 180, c);
        if (tr > 0) drawCorner(m, x + w - tr, y + tr,     tr, 270, c);
        if (br > 0) drawCorner(m, x + w - br, y + h - br, br,   0, c);
        if (bl > 0) drawCorner(m, x + bl,     y + h - bl, bl,  90, c);
    }

    // ── Примитивы ─────────────────────────────────────────────────────────

    public static void drawRectRaw(Matrix4f m, float x, float y, float w, float h, Color c) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float r = c.getRed()/255f, g = c.getGreen()/255f, b = c.getBlue()/255f, a = c.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y,     0).color(r, g, b, a);
        buf.vertex(m, x,     y,     0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    private static void drawCorner(Matrix4f m, float cx, float cy, float r, float deg, Color c) {
        if (r <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = c.getRed()/255f, g = c.getGreen()/255f, b = c.getBlue()/255f, a = c.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m, cx, cy, 0).color(red, g, b, a);
        for (int i = 0; i <= 20; i++) {
            double angle = Math.toRadians(deg + 90.0 * i / 20);
            buf.vertex(m, cx + (float)Math.cos(angle) * r, cy + (float)Math.sin(angle) * r, 0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════════════
    // GLOW
    // ══════════════════════════════════════════════════════════════════════

    public static void drawGlow(Matrix4f m, float x, float y, float w, float h,
                                float r, int spread, Color color) {
        for (int i = spread; i >= 1; i--) {
            float t     = (float) i / spread;
            float alpha = color.getAlpha() * (1f - t) * (1f - t) * 0.9f;
            if (alpha < 1) continue;
            float ex = spread - i + 1;
            drawRect(m, x - ex, y - ex, w + ex*2, h + ex*2, r + ex,
                new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) Math.min(255, alpha)));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // OUTLINE
    // ══════════════════════════════════════════════════════════════════════

    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h,
                                   float r, Color color, float thickness) {
        Matrix4f m = ms.peek().getPositionMatrix();
        for (float t = 0; t < thickness; t++) {
            int alpha = (int)(color.getAlpha() * (1f - t / (thickness + 1)));
            drawOutlineRaw(m, x - t, y - t, w + t*2, h + t*2, r + t,
                new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha)));
        }
    }

    private static void drawOutlineRaw(Matrix4f m, float x, float y, float w, float h, float r, Color c) {
        drawRectRaw(m, x + r,     y,         w - r*2, 1,       c);
        drawRectRaw(m, x + r,     y + h - 1, w - r*2, 1,       c);
        drawRectRaw(m, x,         y + r,     1,       h - r*2, c);
        drawRectRaw(m, x + w - 1, y + r,     1,       h - r*2, c);
        drawArc(m, x + r,     y + r,     r, 180, c);
        drawArc(m, x + w - r, y + r,     r, 270, c);
        drawArc(m, x + w - r, y + h - r, r,   0, c);
        drawArc(m, x + r,     y + h - r, r,  90, c);
    }

    private static void drawArc(Matrix4f m, float cx, float cy, float r, float deg, Color c) {
        if (r <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = c.getRed()/255f, g = c.getGreen()/255f, b = c.getBlue()/255f, a = c.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 20; i++) {
            double angle = Math.toRadians(deg + 90.0 * i / 20);
            float cos = (float)Math.cos(angle), sin = (float)Math.sin(angle);
            buf.vertex(m, cx + cos*(r-1), cy + sin*(r-1), 0).color(red, g, b, a);
            buf.vertex(m, cx + cos*r,     cy + sin*r,     0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════════════
    // GRADIENTS
    // ══════════════════════════════════════════════════════════════════════

    public static void drawGradientV(DrawContext ctx, float x, float y, float w, float h,
                                     Color top, Color bottom) {
        ctx.fillGradient((int)x, (int)y, (int)(x+w), (int)(y+h), top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientH(Matrix4f m, float x, float y, float w, float h,
                                     Color left, Color right) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y+h, 0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x+w,   y+h, 0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x+w,   y,   0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x,     y,   0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    /** Горизонтальный градиент с разными цветами через DrawContext */
    public static void drawGradientH(DrawContext ctx, float x, float y, float w, float h,
                                     Color left, Color right) {
        drawGradientH(ctx.getMatrices().peek().getPositionMatrix(), x, y, w, h, left, right);
    }

    // ══════════════════════════════════════════════════════════════════════
    // CIRCLE
    // ══════════════════════════════════════════════════════════════════════

    public static void drawCircle(Matrix4f m, float cx, float cy, int segments, float r, Color c) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = c.getRed()/255f, g = c.getGreen()/255f, b = c.getBlue()/255f, a = c.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m, cx, cy, 0).color(red, g, b, a);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(360.0 * i / segments);
            buf.vertex(m, cx + (float)Math.cos(angle)*r, cy + (float)Math.sin(angle)*r, 0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEXT
    // ══════════════════════════════════════════════════════════════════════

    public static void drawText(DrawContext ctx, Text text, float x, float y, Color c, boolean shadow) {
        ctx.drawText(Fonts.get(), text, (int)x, (int)y, c.getRGB(), shadow);
    }

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

    // ══════════════════════════════════════════════════════════════════════
    // RAINBOW HELPER
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Возвращает rainbow цвет по времени + смещению (0..1).
     * speed — чем больше, тем медленнее (делитель миллисекунд).
     */
    public static Color getRainbow(float offset, float speed, float saturation, float brightness, int alpha) {
        float hue = ((System.currentTimeMillis() % (long)(speed * 1000)) / (speed * 1000f) + offset) % 1f;
        Color c = Color.getHSBColor(hue, saturation, brightness);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}
