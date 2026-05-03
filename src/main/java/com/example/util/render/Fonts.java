package com.example.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Обёртка над кастомными шрифтами Inter из assets/modid/fonts/
 * JSON-файлы: inter_bold.json, inter_medium.json, inter_regular.json
 * определяют шрифты с идентификаторами modid:inter_bold и т.д.
 */
public class Fonts {

    // Идентификаторы совпадают с именами JSON файлов в assets/modid/fonts/
    public static final Identifier BOLD    = Identifier.of("modid", "inter_bold");
    public static final Identifier MEDIUM  = Identifier.of("modid", "inter_medium");
    public static final Identifier REGULAR = Identifier.of("modid", "inter_regular");

    public static TextRenderer get() {
        return MinecraftClient.getInstance().textRenderer;
    }

    // ── Создание стилизованного Text с нужным шрифтом ─────────────────────
    public static Text bold(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(BOLD));
    }

    public static Text medium(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(MEDIUM));
    }

    public static Text regular(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(REGULAR));
    }

    // ── Ширина текста с нужным шрифтом ────────────────────────────────────
    public static int width(String text, Identifier font) {
        return get().getWidth(Text.literal(text).setStyle(Style.EMPTY.withFont(font)));
    }

    /** Ширина строки шрифтом Medium (дефолт для UI) */
    public static int width(String text) {
        return width(text, MEDIUM);
    }

    public static int widthBold(String text) {
        return width(text, BOLD);
    }

    public static int widthRegular(String text) {
        return width(text, REGULAR);
    }

    /** Высота строки (одинакова для всех шрифтов в Minecraft) */
    public static int height() {
        return get().fontHeight;
    }
}
