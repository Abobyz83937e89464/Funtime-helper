package com.example.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class Fonts {

    public static TextRenderer BOLD;
    public static TextRenderer MEDIUM;
    public static TextRenderer REGULAR;

    public static void init() {
        // В 1.21.4 getFontManager() убран, используем стандартный textRenderer
        // Кастомные шрифты через json грузятся автоматически через fontRenderer по Identifier
        BOLD    = MinecraftClient.getInstance().textRenderer;
        MEDIUM  = MinecraftClient.getInstance().textRenderer;
        REGULAR = MinecraftClient.getInstance().textRenderer;
    }
}
