package com.example.util.render;

import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class DrawHelper {

    public static void drawRect(DrawContext context, float x, float y, float width, float height, float radius, Color color) {
        if (width <= 0 || height <= 0) return;
        int argb = color.getRGB();
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), argb);
    }

    public static void drawRect(DrawContext context, int x, int y, int width, int height, int radius, Color color) {
        if (width <= 0 || height <= 0) return;
        context.fill(x, y, x + width, y + height, color.getRGB());
    }

    public static void drawOutline(DrawContext context, float x, float y, float width, float height, float radius, Color color, float thickness) {
        int t = Math.max(1, (int) thickness);
        int c = color.getRGB();
        int ix = (int) x;
        int iy = (int) y;
        int iw = (int) width;
        int ih = (int) height;

        // Top
        context.fill(ix, iy, ix + iw, iy + t, c);
        // Bottom
        context.fill(ix, iy + ih - t, ix + iw, iy + ih, c);
        // Left
        context.fill(ix, iy, ix + t, iy + ih, c);
        // Right
        context.fill(ix + iw - t, iy, ix + iw, iy + ih, c);
    }
}
