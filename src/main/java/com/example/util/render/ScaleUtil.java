package com.example.util.render;

import net.minecraft.client.MinecraftClient;

public class ScaleUtil {

    public static float getScale() {
        return (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
    }

    public static int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }

    /**
     * Переводит scaled-координаты мыши в реальные пиксельные.
     */
    public static double toRealX(double scaledX) {
        return scaledX * getScale();
    }

    public static double toRealY(double scaledY) {
        return scaledY * getScale();
    }
}
