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

    public static TextRenderer get() {
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

    public static int boldWidth(String text)    { return get().getWidth(bold(text));    }
    public static int mediumWidth(String text)  { return get().getWidth(medium(text));  }
    public static int regularWidth(String text) { return get().getWidth(regular(text)); }

    /** По умолчанию используем bold */
    public static int width(String text)  { return boldWidth(text);      }
    public static int height()            { return get().fontHeight;      }
}
