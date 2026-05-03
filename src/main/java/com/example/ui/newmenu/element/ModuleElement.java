package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ModuleElement {

    // ── Палитра ───────────────────────────────────────────────
    private static final Color CARD_BG      = new Color(0x18, 0x1A, 0x22);
    private static final Color CARD_BG_ON   = new Color(0x1C, 0x1F, 0x30);
    private static final Color ACCENT       = new Color(0x6C, 0x7B, 0xFF);
    private static final Color ACCENT_DIM   = new Color(0x6C, 0x7B, 0xFF, 60);
    private static final Color TEXT_PRIMARY  = new Color(0xE6, 0xE9, 0xFF);
    private static final Color TEXT_SECONDARY= new Color(0x8A, 0x90, 0xC2);
    private static final Color OUTLINE_IDLE  = new Color(0x2A, 0x2C, 0x3E);
    private static final Color OUTLINE_ON    = new Color(0x6C, 0x7B, 0xFF, 120);
    private static final Color TOGGLE_OFF_BG = new Color(0x28, 0x2A, 0x3C);
    private static final Color GREEN_ON      = new Color(0x64, 0xC8, 0x82);

    private final Map<String, Float> hoverAnims  = new HashMap<>();
    private final Map<String, Float> toggleAnims = new HashMap<>();

    public static final float BASE_H = 54f;
    public static final float RADIUS = 8f;

    public float getHeight() { return BASE_H; }
    public static float getModuleWidth() { return 98f; }

    public void render(DrawContext ctx, float x, float y, Menu.ModuleEntry mod, double mx, double my) {
        float w = getModuleWidth();
        MatrixStack ms = ctx.getMatrices();

        // Анимации
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + BASE_H;
        float ha = lerp(hoverAnims.getOrDefault(mod.name, 0f), hov ? 1f : 0f, 0.14f);
        hoverAnims.put(mod.name, ha);

        float ta = lerp(toggleAnims.getOrDefault(mod.name, mod.enabled ? 1f : 0f), mod.enabled ? 1f : 0f, 0.12f);
        toggleAnims.put(mod.name, ta);

        // ── Тень под карточкой ────────────────────────────────
        DrawHelper.drawShadow(ms, x, y, w, BASE_H, RADIUS, 8f,
            new Color(0, 0, 0, (int)(140 * (0.5f + 0.5f * ta))));

        // ── Фон карточки ──────────────────────────────────────
        Color bg = lerpColor(CARD_BG, CARD_BG_ON, ta);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, y, w, BASE_H, RADIUS, bg);

        // Hover overlay (фиолетовый, не белый)
        if (ha > 0.01f)
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, y, w, BASE_H, RADIUS,
                new Color(108, 123, 255, (int)(30 * ha)));

        // ── Outline ───────────────────────────────────────────
        Color outline = lerpColor(OUTLINE_IDLE, OUTLINE_ON, ta);
        DrawHelper.drawOutline(ms, x - 1, y - 1, w + 2, BASE_H + 2, RADIUS, outline, 1);

        // ── Акцент сверху (включён) ───────────────────────────
        if (ta > 0.01f) {
            float lw = (w - RADIUS * 2) * ta;
            // Glow за линией
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + RADIUS, y, lw, 4, 0,
                new Color(108, 123, 255, (int)(35 * ta)));
            // Сама линия
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + RADIUS, y, lw, 2, 0,
                new Color(108, 123, 255, (int)(230 * ta)));
        }

        // ── Название (Inter Bold) ─────────────────────────────
        Color nameCol = lerpColor(TEXT_SECONDARY, TEXT_PRIMARY, ta);
        DrawHelper.drawText(ctx, mod.name, x + 8, y + 8, nameCol);

        // ── Toggle ────────────────────────────────────────────
        float tw = 28f, th = 14f;
        float tx = x + w - tw - 7;
        float ty = y + BASE_H - th - 8;

        // Фон тоггла
        Color toggleBg = lerpColor(TOGGLE_OFF_BG, ACCENT, ta);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), tx, ty, tw, th, th / 2f, toggleBg);

        // Knob
        float cs = th - 4f;
        float kx  = tx + 2 + ta * (tw - cs - 4);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), kx, ty + 2, cs, cs, cs / 2f,
            new Color(255, 255, 255, (int)(180 + 75 * ta)));

        // ── Статус текст ──────────────────────────────────────
        Color statusC = lerpColor(TEXT_SECONDARY, GREEN_ON, ta);
        DrawHelper.drawTextMedium(ctx, mod.enabled ? "Enabled" : "Disabled",
            x + 8, ty + (th - Fonts.height()) / 2f, statusC);
    }

    public boolean mouseClicked(float x, float y, Menu.ModuleEntry mod, double mx, double my, int button) {
        if (button == 0 && mx >= x && mx <= x + getModuleWidth() && my >= y && my <= y + BASE_H) {
            mod.enabled = !mod.enabled;
            return true;
        }
        return false;
    }

    private float lerp(float from, float to, float t) { return from + (to - from) * t; }

    private Color lerpColor(Color a, Color b, float t) {
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t),
            (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }
}
