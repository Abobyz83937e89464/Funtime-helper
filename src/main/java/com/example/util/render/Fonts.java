package com.example.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class Fonts {

    private static TextRenderer _bold;
    private static TextRenderer _medium;
    private static TextRenderer _regular;

    public static TextRenderer getBold() {
        if (_bold == null) _bold = MinecraftClient.getInstance().textRenderer;
        return _bold;
    }

    public static TextRenderer getMedium() {
        if (_medium == null) _medium = MinecraftClient.getInstance().textRenderer;
        return _medium;
    }

    public static TextRenderer getRegular() {
        if (_regular == null) _regular = MinecraftClient.getInstance().textRenderer;
        return _regular;
    }
}
