package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.awt.*;

public class DrawHelper {

    public static void drawRect(DrawContext context, float x, float y, float width, float height, int color) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
    }

    public static void drawGradientRect(DrawContext context, float x, float y, float width, float height, int startColor, int endColor) {
        context.fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), startColor, endColor);
    }

    // Позже сюда добавим методы для скругления углов и теней
}
