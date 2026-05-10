package com.example.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Fonts {

    public static final Identifier BOLD    = Identifier.of("nocturn-client", "inter_bold");
    public static final Identifier MEDIUM  = Identifier.of("nocturn-client", "inter_medium");
    public static final Identifier REGULAR = Identifier.of("nocturn-client", "inter_regular");

    public static TextRenderer renderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static Text bold(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(BOLD));
    }

    public static Text medium(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(MEDIUM));
    }

    public static Text regular(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(REGULAR));
    }

    public static int boldWidth(String text)    { return renderer().getWidth(bold(text)); }
    public static int mediumWidth(String text)  { return renderer().getWidth(medium(text)); }
    public static int regularWidth(String text) { return renderer().getWidth(regular(text)); }

    // ✅ ФИКС: width() теперь возвращает mediumWidth — drawText() рендерит medium
    public static int width(String text)  { return mediumWidth(text); }
    public static int height()            { return renderer().fontHeight; }
    public static TextRenderer get()      { return renderer(); }
}
