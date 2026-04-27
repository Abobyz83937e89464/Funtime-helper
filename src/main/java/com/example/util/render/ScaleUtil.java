package com.example.util.render;

import net.minecraft.client.MinecraftClient;

public class ScaleUtil {
    public static float getScale() {
        return (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
    }
}
