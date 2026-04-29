package com.example.util.render;

import net.minecraft.client.gui.DrawContext;
import java.awt.Color;

public class DrawHelper {

    public static void drawRect(DrawContext context, int x, int y, int width, int height, Color color) {
        if (width <= 0 || height <= 0) return;
        context.fill(x, y, x + width, y + height, color.getRGB());
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, Color color) {
        drawRect(context, (int) x, (int) y, (int) width, (int) height, color);
    }

    public static void drawOutlineRect(DrawContext context, int x, int y, int width, int height, Color color, int thickness) {
        // Top
        drawRect(context, x, y, width, thickness, color);
        // Bottom
        drawRect(context, x, y + height - thickness, width, thickness, color);
        // Left
        drawRect(context, x, y, thickness, height, color);
        // Right
        drawRect(context, x + width - thickness, y, thickness, height, color);
    }

    public static void drawGradientRect(DrawContext context, int x, int y, int width, int height, Color colorTop, Color colorBottom) {
        if (width <= 0 || height <= 0) return;
        context.fillGradient(x, y, x + width, y + height, colorTop.getRGB(), colorBottom.getRGB());
    }

    public static void drawShadow(DrawContext context, int x, int y, int width, int height, int shadowSize) {
        for (int i = 0; i < shadowSize; i++) {
            int alpha = (int) (40 * (1.0f - (float) i / shadowSize));
            Color shadowColor = new Color(0, 0, 0, Math.max(alpha, 0));
            drawOutlineRect(context, x - i - 1, y - i - 1, width + (i + 1) * 2, height + (i + 1) * 2, shadowColor, 1);
        }
    }
}
