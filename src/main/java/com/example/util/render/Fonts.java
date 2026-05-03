package com.example.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class Fonts {
    public static TextRenderer get() {
        return MinecraftClient.getInstance().textRenderer;
    }
    public static int width(String text) { return get().getWidth(text); }
    public static int height()           { return get().fontHeight; }
}
