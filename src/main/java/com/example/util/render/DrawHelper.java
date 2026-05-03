package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    // ─────────────────────────────────────────────────────────────
    //  RECT (скруглённый)
    // ─────────────────────────────────────────────────────────────

    public static void drawRect(MatrixStack ms, float x, float y, float w, float h, float r, Color c) {
        drawRect(ms.peek().getPositionMatrix(), x, y, w, h, r, c);
    }

    public static void drawRect(Matrix4f m, float x, float y, float w, float h, float r, Color c) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w / 2f, h / 2f));
        drawRectRaw(m, x + r,     y,         w - r * 2, h,         c);
        drawRectRaw(m, x,         y + r,     r,         h - r * 2, c);
        drawRectRaw(m, x + w - r, y + r,     r,         h - r * 2, c);
        drawCorner (m, x + r,     y + r,     r, 180, c);
        drawCorner (m, x + w - r, y + r,     r, 270, c);
        drawCorner (m, x + w - r, y + h - r, r, 0,   c);
        drawCorner (m, x + r,     y + h - r, r, 90,  c);
    }

    // простой fill через DrawContext (без скруглений, но без создания нового контекста!)
    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color c) {
        if (w <= 0 || h <= 0) return;
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), c.getRGB());
    }

    private static void drawRectRaw(Matrix4f m, float x, float y, float w, float h, Color c) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float r = c.getRed() / 255f, g = c.getGreen() / 255f,
              b = c.getBlue() / 255f, a = c.getAlpha() / 255f;
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
        float red = c.getRed() / 255f, g = c.getGreen() / 255f,
              b   = c.getBlue() / 255f, a = c.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m, cx, cy, 0).color(red, g, b, a);
        for (int i = 0; i <= 12; i++) {
            double angle = Math.toRadians(deg + 90.0 * i / 12);
            buf.vertex(m, cx + (float) Math.cos(angle) * r, cy + (float) Math.sin(angle) * r, 0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ─────────────────────────────────────────────────────────────
    //  STYLED RECT (разные радиусы углов: tl, tr, br, bl)
    // ─────────────────────────────────────────────────────────────

    public static void drawStyledRect(Matrix4f m, float x, float y, float w, float h,
                                      float tl, float tr, float br, float bl, Color c) {
        // центр
        drawRectRaw(m, x + tl, y + tl, w - tl - tr, h - tl - bl, c);
        // верхняя полоса без углов
        if (tl + tr < w) drawRectRaw(m, x + tl, y, w - tl - tr, Math.max(tl, tr), c);
        // нижняя полоса
        if (bl + br < w) drawRectRaw(m, x + bl, y + h - Math.max(bl, br), w - bl - br, Math.max(bl, br), c);
        // левая полоса
        if (tl + bl < h) drawRectRaw(m, x, y + tl, Math.max(tl, bl), h - tl - bl, c);
        // правая полоса
        if (tr + br < h) drawRectRaw(m, x + w - Math.max(tr, br), y + tr, Math.max(tr, br), h - tr - br, c);
        // углы
        if (tl > 0) drawCorner(m, x + tl,     y + tl,     tl, 180, c);
        if (tr > 0) drawCorner(m, x + w - tr, y + tr,     tr, 270, c);
        if (br > 0) drawCorner(m, x + w - br, y + h - br, br, 0,   c);
        if (bl > 0) drawCorner(m, x + bl,     y + h - bl, bl, 90,  c);
    }

    // ─────────────────────────────────────────────────────────────
    //  OUTLINE
    // ─────────────────────────────────────────────────────────────

    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h,
                                   float r, Color c, float thickness) {
        Matrix4f m = ms.peek().getPositionMatrix();
        for (float t = 0; t < thickness; t++) {
            float alpha = c.getAlpha() * (1f - t / thickness);
            Color col = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) alpha);
            drawOutlineRaw(m, x - t, y - t, w + t * 2, h + t * 2, r + t, col);
        }
    }

    private static void drawOutlineRaw(Matrix4f m, float x, float y, float w, float h, float r, Color c) {
        drawRectRaw(m, x + r,     y,         w - r * 2, 1,         c);
        drawRectRaw(m, x + r,     y + h - 1, w - r * 2, 1,         c);
        drawRectRaw(m, x,         y + r,     1,         h - r * 2, c);
        drawRectRaw(m, x + w - 1, y + r,     1,         h - r * 2, c);
        drawArc(m, x + r,     y + r,     r, 180, c);
        drawArc(m, x + w - r, y + r,     r, 270, c);
        drawArc(m, x + w - r, y + h - r, r, 0,   c);
        drawArc(m, x + r,     y + h - r, r, 90,  c);
    }

    private static void drawArc(Matrix4f m, float cx, float cy, float r, float deg, Color c) {
        if (r <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = c.getRed() / 255f, g = c.getGreen() / 255f,
              b   = c.getBlue() / 255f, a = c.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 12; i++) {
            double angle = Math.toRadians(deg + 90.0 * i / 12);
            float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
            buf.vertex(m, cx + cos * (r - 1), cy + sin * (r - 1), 0).color(red, g, b, a);
            buf.vertex(m, cx + cos * r,       cy + sin * r,       0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ─────────────────────────────────────────────────────────────
    //  GRADIENT
    // ─────────────────────────────────────────────────────────────

    public static void drawGradientV(DrawContext ctx, float x, float y, float w, float h, Color top, Color bottom) {
        ctx.fillGradient((int) x, (int) y, (int) (x + w), (int) (y + h), top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientH(Matrix4f m, float x, float y, float w, float h, Color left, Color right) {
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

    // ─────────────────────────────────────────────────────────────
    //  CIRCLE
    // ─────────────────────────────────────────────────────────────

    public static void drawCircle(Matrix4f m, float cx, float cy, int segments, float r, Color c) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = c.getRed() / 255f, g = c.getGreen() / 255f,
              b   = c.getBlue() / 255f, a = c.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m, cx, cy, 0).color(red, g, b, a);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(360.0 * i / segments);
            buf.vertex(m, cx + (float) Math.cos(angle) * r, cy + (float) Math.sin(angle) * r, 0).color(red, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ─────────────────────────────────────────────────────────────
    //  TEXT — через кастомные шрифты (Fonts.bold/medium/regular)
    // ─────────────────────────────────────────────────────────────

    /** Рисует текст через Inter Bold */
    public static void drawText(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.renderer(), Fonts.bold(text), (int) x, (int) y, c.getRGB(), false);
    }

    /** Рисует текст с тенью через Inter Bold */
    public static void drawTextShadow(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.renderer(), Fonts.bold(text), (int) x, (int) y, c.getRGB(), true);
    }

    /** Рисует текст через Inter Medium */
    public static void drawTextMedium(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.renderer(), Fonts.medium(text), (int) x, (int) y, c.getRGB(), false);
    }

    /** Рисует текст через Inter Regular */
    public static void drawTextRegular(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.renderer(), Fonts.regular(text), (int) x, (int) y, c.getRGB(), false);
    }

    // legacy-метод для совместимости со старым кодом (MatrixStack + TextRenderer)
    public static void drawText(Matrix4f m, net.minecraft.client.font.TextRenderer font,
                                 String text, float x, float y, Color c) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.textRenderer.draw(
            text, x, y, c.getRGB(), false, m,
            mc.getBufferBuilders().getEntityVertexConsumers(),
            net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
            0, 0xF000F0
        );
        mc.getBufferBuilders().getEntityVertexConsumers().draw();
    }
}
