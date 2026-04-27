package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    public static void drawRect(DrawContext context, float x, float y, float width, float height, float radius, Color color) {
        if (width <= 0 || height <= 0) return;

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        if (radius <= 0) {
            buffer.vertex(matrix, x, y + height, 0).color(r, g, b, a);
            buffer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a);
            buffer.vertex(matrix, x + width, y, 0).color(r, g, b, a);
            buffer.vertex(matrix, x, y, 0).color(r, g, b, a);
        } else {
            drawRoundedRectInternal(buffer, matrix, x, y, width, height, radius, r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    private static void drawRoundedRectInternal(BufferBuilder buffer, Matrix4f matrix,
                                                  float x, float y, float w, float h,
                                                  float radius, float r, float g, float b, float a) {
        // Центр
        buffer.vertex(matrix, x + radius, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y + radius, 0).color(r, g, b, a);

        // Верх
        buffer.vertex(matrix, x + radius, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y, 0).color(r, g, b, a);

        // Низ
        buffer.vertex(matrix, x + radius, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y + h - radius, 0).color(r, g, b, a);

        // Лево
        buffer.vertex(matrix, x, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x, y + radius, 0).color(r, g, b, a);

        // Право
        buffer.vertex(matrix, x + w - radius, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y + radius, 0).color(r, g, b, a);

        // Углы (упрощённо — квадратами, для скругления нужен shader)
        // Верх-лево
        buffer.vertex(matrix, x, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y, 0).color(r, g, b, a);
        buffer.vertex(matrix, x, y, 0).color(r, g, b, a);

        // Верх-право
        buffer.vertex(matrix, x + w - radius, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w, y + radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w, y, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y, 0).color(r, g, b, a);

        // Низ-лево
        buffer.vertex(matrix, x, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + radius, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x, y + h - radius, 0).color(r, g, b, a);

        // Низ-право
        buffer.vertex(matrix, x + w - radius, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w, y + h - radius, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + w - radius, y + h - radius, 0).color(r, g, b, a);
    }

    public static void drawOutline(DrawContext context, float x, float y, float width, float height, float radius, Color color, float thickness) {
        drawRect(context, x, y, width, thickness, 0, color); // top
        drawRect(context, x, y + height - thickness, width, thickness, 0, color); // bottom
        drawRect(context, x, y, thickness, height, 0, color); // left
        drawRect(context, x + width - thickness, y, thickness, height, 0, color); // right
    }
}
