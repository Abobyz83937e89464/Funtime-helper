package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, int color) {
        if (w <= 0 || h <= 0) return;
        ctx.fill((int)x, (int)y, (int)(x+w), (int)(y+h), color);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color color) {
        drawRect(ctx, x, y, w, h, color.getRGB());
    }

    public static void drawGradientV(DrawContext ctx, float x, float y, float w, float h, Color top, Color bottom) {
        if (w <= 0 || h <= 0) return;
        ctx.fillGradient((int)x, (int)y, (int)(x+w), (int)(y+h), top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientH(DrawContext ctx, float x, float y, float w, float h, Color left, Color right) {
        if (w <= 0 || h <= 0) return;
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,   y+h, 0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x+w, y+h, 0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x+w, y,   0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x,   y,   0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(DrawContext ctx, float x, float y, float w, float h, float r, Color color) {
        if (r <= 0) { drawRect(ctx, x, y, w, h, color); return; }
        drawRect(ctx, x+r, y,   w-r*2, h,   color);
        drawRect(ctx, x,   y+r, r,     h-r*2, color);
        drawRect(ctx, x+w-r, y+r, r,   h-r*2, color);
        drawCorner(ctx, x+r,   y+r,   r, 180, color);
        drawCorner(ctx, x+w-r, y+r,   r, 270, color);
        drawCorner(ctx, x+w-r, y+h-r, r, 0,   color);
        drawCorner(ctx, x+r,   y+h-r, r, 90,  color);
    }

    private static void drawCorner(DrawContext ctx, float cx, float cy, float r, float startDeg, Color color) {
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = color.getRed()/255f, green = color.getGreen()/255f,
              blue = color.getBlue()/255f, alpha = color.getAlpha()/255f;
        int seg = 10;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m, cx, cy, 0).color(red, green, blue, alpha);
        for (int i = 0; i <= seg; i++) {
            double a = Math.toRadians(startDeg + 90.0*i/seg);
            buf.vertex(m, cx+(float)Math.cos(a)*r, cy+(float)Math.sin(a)*r, 0).color(red, green, blue, alpha);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    public static void drawOutline(DrawContext ctx, float x, float y, float w, float h, float r, Color color) {
        drawRect(ctx, x+r, y,       w-r*2, 1, color);
        drawRect(ctx, x+r, y+h-1,   w-r*2, 1, color);
        drawRect(ctx, x,   y+r,     1,     h-r*2, color);
        drawRect(ctx, x+w-1, y+r,   1,     h-r*2, color);
        drawArcOutline(ctx, x+r,   y+r,   r, 180, color);
        drawArcOutline(ctx, x+w-r, y+r,   r, 270, color);
        drawArcOutline(ctx, x+w-r, y+h-r, r, 0,   color);
        drawArcOutline(ctx, x+r,   y+h-r, r, 90,  color);
    }

    private static void drawArcOutline(DrawContext ctx, float cx, float cy, float r, float startDeg, Color color) {
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red = color.getRed()/255f, green = color.getGreen()/255f,
              blue = color.getBlue()/255f, alpha = color.getAlpha()/255f;
        int seg = 10;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= seg; i++) {
            double a = Math.toRadians(startDeg + 90.0*i/seg);
            float cos = (float)Math.cos(a), sin = (float)Math.sin(a);
            buf.vertex(m, cx+cos*(r-1), cy+sin*(r-1), 0).color(red, green, blue, alpha);
            buf.vertex(m, cx+cos*r,     cy+sin*r,     0).color(red, green, blue, alpha);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    public static void drawText(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), text, (int)x, (int)y, color.getRGB(), false);
    }

    public static void drawTextShadow(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), text, (int)x, (int)y, color.getRGB(), true);
    }

    public static int textWidth(String text) {
        return Fonts.get().getWidth(text);
    }

    public static int textHeight() {
        return Fonts.get().fontHeight;
    }
}
