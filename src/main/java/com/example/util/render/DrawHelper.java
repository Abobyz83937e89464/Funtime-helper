package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    public static void drawRect(DrawContext context, float x, float y, float width, float height, Color color) {
        if (width <= 0 || height <= 0) return;
        context.fill((int) x, (int) y, (int)(x + width), (int)(y + height), color.getRGB());
    }

    public static void drawGradientRect(DrawContext context, float x, float y, float width, float height, Color top, Color bottom) {
        if (width <= 0 || height <= 0) return;
        context.fillGradient((int) x, (int) y, (int)(x + width), (int)(y + height), top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientRectH(DrawContext context, float x, float y, float width, float height, Color left, Color right) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x,         y + height, 0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        buffer.vertex(matrix, x + width, y + height, 0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buffer.vertex(matrix, x + width, y,          0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buffer.vertex(matrix, x,         y,          0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(DrawContext context, float x, float y, float width, float height, float radius, Color color) {
        if (radius <= 0) { drawRect(context, x, y, width, height, color); return; }
        drawRect(context, x + radius,         y,          width - radius * 2, height,              color);
        drawRect(context, x,                  y + radius, radius,             height - radius * 2, color);
        drawRect(context, x + width - radius, y + radius, radius,             height - radius * 2, color);
        drawCircleQuarter(context, x + radius,         y + radius,          radius, 180, color);
        drawCircleQuarter(context, x + width - radius, y + radius,          radius, 270, color);
        drawCircleQuarter(context, x + width - radius, y + height - radius, radius, 0,   color);
        drawCircleQuarter(context, x + radius,         y + height - radius, radius, 90,  color);
    }

    private static void drawCircleQuarter(DrawContext context, float cx, float cy, float r, float startDeg, Color color) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        int segments = 12;
        float red = color.getRed() / 255f, green = color.getGreen() / 255f,
              blue = color.getBlue() / 255f, alpha = color.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(matrix, cx, cy, 0).color(red, green, blue, alpha);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(startDeg + 90.0 * i / segments);
            buf.vertex(matrix, cx + (float)Math.cos(angle) * r, cy + (float)Math.sin(angle) * r, 0).color(red, green, blue, alpha);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    public static void drawRoundedOutline(DrawContext context, float x, float y, float width, float height, float radius, Color color, float thickness) {
        drawRect(context, x + radius,            y,                      width - radius * 2, thickness,           color);
        drawRect(context, x + radius,            y + height - thickness, width - radius * 2, thickness,           color);
        drawRect(context, x,                     y + radius,             thickness,          height - radius * 2, color);
        drawRect(context, x + width - thickness, y + radius,             thickness,          height - radius * 2, color);
        drawArcOutline(context, x + radius,         y + radius,          radius, 180, color, thickness);
        drawArcOutline(context, x + width - radius, y + radius,          radius, 270, color, thickness);
        drawArcOutline(context, x + width - radius, y + height - radius, radius, 0,   color, thickness);
        drawArcOutline(context, x + radius,         y + height - radius, radius, 90,  color, thickness);
    }

    private static void drawArcOutline(DrawContext context, float cx, float cy, float r, float startDeg, Color color, float thickness) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        int segments = 12;
        float red = color.getRed() / 255f, green = color.getGreen() / 255f,
              blue = color.getBlue() / 255f, alpha = color.getAlpha() / 255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(startDeg + 90.0 * i / segments);
            float cos = (float)Math.cos(angle), sin = (float)Math.sin(angle);
            buf.vertex(matrix, cx + cos * (r - thickness), cy + sin * (r - thickness), 0).color(red, green, blue, alpha);
            buf.vertex(matrix, cx + cos * r,               cy + sin * r,               0).color(red, green, blue, alpha);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    public static void drawShadow(DrawContext context, float x, float y, float width, float height, int layers) {
        for (int i = layers; i > 0; i--) {
            int a = (int)(55.0f * ((float)(layers - i + 1) / layers));
            drawRoundedOutline(context, x - i, y - i, width + i * 2, height + i * 2, 10 + i, new Color(0, 0, 0, Math.max(a, 0)), 1);
        }
    }

    public static void drawText(DrawContext context, TextRenderer font, String text, float x, float y, Color color) {
        context.drawText(font, text, (int) x, (int) y, color.getRGB(), false);
    }

    public static void drawTextShadow(DrawContext context, TextRenderer font, String text, float x, float y, Color color) {
        context.drawText(font, text, (int) x, (int) y, color.getRGB(), true);
    }
}
