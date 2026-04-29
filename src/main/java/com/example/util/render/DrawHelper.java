package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    // ─── Базовые прямоугольники ───────────────────────────────────────────────

    public static void drawRect(DrawContext context, int x, int y, int width, int height, Color color) {
        if (width <= 0 || height <= 0) return;
        context.fill(x, y, x + width, y + height, color.getRGB());
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, Color color) {
        drawRect(context, (int) x, (int) y, (int) width, (int) height, color);
    }

    // ─── Outline ────────────────────────────────────────────────────────────

    public static void drawOutlineRect(DrawContext context, int x, int y, int width, int height, Color color, int thickness) {
        drawRect(context, x, y, width, thickness, color);
        drawRect(context, x, y + height - thickness, width, thickness, color);
        drawRect(context, x, y, thickness, height, color);
        drawRect(context, x + width - thickness, y, thickness, height, color);
    }

    public static void drawOutlineRect(DrawContext context, float x, float y, float width, float height, Color color, int thickness) {
        drawOutlineRect(context, (int) x, (int) y, (int) width, (int) height, color, thickness);
    }

    // ─── Градиент ────────────────────────────────────────────────────────────

    public static void drawGradientRect(DrawContext context, int x, int y, int width, int height, Color colorTop, Color colorBottom) {
        if (width <= 0 || height <= 0) return;
        context.fillGradient(x, y, x + width, y + height, colorTop.getRGB(), colorBottom.getRGB());
    }

    // ─── Горизонтальный градиент ─────────────────────────────────────────────

    public static void drawGradientRectH(DrawContext context, int x, int y, int width, int height, Color colorLeft, Color colorRight) {
        if (width <= 0 || height <= 0) return;

        MatrixStack matrixStack = context.getMatrices();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int cl = colorLeft.getRGB();
        int cr = colorRight.getRGB();

        buffer.vertex(matrix, x,         y + height, 0).color(cl);
        buffer.vertex(matrix, x + width, y + height, 0).color(cr);
        buffer.vertex(matrix, x + width, y,          0).color(cr);
        buffer.vertex(matrix, x,         y,          0).color(cl);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    // ─── Тень ────────────────────────────────────────────────────────────────

    public static void drawShadow(DrawContext context, int x, int y, int width, int height, int shadowSize) {
        for (int i = 0; i < shadowSize; i++) {
            int alpha = (int) (50 * (1.0f - (float) i / shadowSize));
            Color shadowColor = new Color(0, 0, 0, Math.max(alpha, 0));
            drawOutlineRect(context, x - i - 1, y - i - 1,
                    width + (i + 1) * 2, height + (i + 1) * 2, shadowColor, 1);
        }
    }

    // ─── Скруглённые углы (простая имитация через несколько rect) ────────────

    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, Color color) {
        // Центральные полосы
        drawRect(context, x + radius, y, width - radius * 2, height, color);
        drawRect(context, x, y + radius, radius, height - radius * 2, color);
        drawRect(context, x + width - radius, y + radius, radius, height - radius * 2, color);
        // Заполняем углы кругами (приближение квадратами по кольцам)
        for (int dx = 0; dx < radius; dx++) {
            for (int dy = 0; dy < radius; dy++) {
                double dist = Math.sqrt((radius - dx - 0.5) * (radius - dx - 0.5) + (radius - dy - 0.5) * (radius - dy - 0.5));
                if (dist <= radius) {
                    // Верхний левый
                    drawRect(context, x + dx, y + dy, 1, 1, color);
                    // Верхний правый
                    drawRect(context, x + width - 1 - dx, y + dy, 1, 1, color);
                    // Нижний левый
                    drawRect(context, x + dx, y + height - 1 - dy, 1, 1, color);
                    // Нижний правый
                    drawRect(context, x + width - 1 - dx, y + height - 1 - dy, 1, 1, color);
                }
            }
        }
    }

    // ─── Акцентная полоска (горизонтальная с градиентом) ─────────────────────

    public static void drawAccentBar(DrawContext context, int x, int y, int width, int height) {
        drawGradientRectH(context, x, y, width / 2, height,
                new Color(80, 100, 255, 200), new Color(125, 136, 255, 200));
        drawGradientRectH(context, x + width / 2, y, width / 2, height,
                new Color(125, 136, 255, 200), new Color(80, 100, 255, 0));
    }
}
