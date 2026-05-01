package com.example.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;

public class Fonts {

    public static TextRenderer BOLD;
    public static TextRenderer MEDIUM;
    public static TextRenderer REGULAR;

    public static void init() {
        BOLD = MinecraftClient.getInstance().textRenderer;
        MEDIUM = MinecraftClient.getInstance().textRenderer;
        REGULAR = MinecraftClient.getInstance().textRenderer;

        try {
            var fontManager = MinecraftClient.getInstance().getFontManager();
            BOLD = fontManager.getRenderer(Identifier.of("modid", "inter_bold"));
            MEDIUM = fontManager.getRenderer(Identifier.of("modid", "inter_medium"));
            REGULAR = fontManager.getRenderer(Identifier.of("modid", "inter_regular"));
        } catch (Exception e) {
            // fallback на стандартный если что-то пошло не так
            BOLD = MinecraftClient.getInstance().textRenderer;
            MEDIUM = MinecraftClient.getInstance().textRenderer;
            REGULAR = MinecraftClient.getInstance().textRenderer;
        }
    }
}
