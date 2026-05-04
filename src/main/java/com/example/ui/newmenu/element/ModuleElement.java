package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ModuleElement {

    private static final float NAME_GAP = 3f;
    private static final float PANEL_H  = 34f;

    private final Map<String, Float> hoverAnims  = new HashMap<>();
    private final Map<String, Float> toggleAnims = new HashMap<>();

    // Индекс модуля для смещения rainbow
    private final Map<String, Integer> moduleIndex = new HashMap<>();
    private int indexCounter = 0;

    public float getHeight() {
        return Fonts.height() + NAME_GAP + PANEL_H;
    }

    public void render(DrawContext ctx, MatrixStack ms,
                       float x, float y, float modW,
                       Menu.ModuleEntry mod, double mx, double my) {
        Matrix4f m = ms.peek().getPositionMatrix();

        // Присваиваем индекс модулю (для смещения радуги)
        moduleIndex.computeIfAbsent(mod.name, k -> indexCounter++);
        int idx = moduleIndex.getOrDefault(mod.name, 0);

        // Анимации
        boolean hovered = mx >= x && mx <= x+modW && my >= y && my <= y+getHeight();
        float ha = lerp(hoverAnims.getOrDefault(mod.name, 0f),  hovered       ? 1f : 0f, 0.16f);
        float ta = lerp(toggleAnims.getOrDefault(mod.name, 0f), mod.enabled   ? 1f : 0f, 0.13f);
        hoverAnims.put(mod.name, ha);
        toggleAnims.put(mod.name, ta);

        float panelY = y + Fonts.height() + NAME_GAP;
        float rad    = 6f;

        // ── Rainbow цвет акцента для этого модуля ─────────────────────────
        // offset = idx * 0.08f даёт каждому модулю свой сдвиг по радуге
        float  rainbowOffset = idx * 0.065f;
        Color  rainbow       = DrawHelper.getRainbow(rainbowOffset, 4f, 0.55f, 1f, 255);
        Color  rainbowDim    = new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), (int)(80 * ta));
        Color  rainbowLine   = new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), (int)(220 * ta));
        Color  rainbowGlow   = new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), (int)(30 * ta));

        // ── Название модуля ───────────────────────────────────────────────
        // Тоже с плавным переходом к rainbow при включении
        Color baseNameColor = new Color(100, 103, 145);
        Color onNameColor   = new Color(
            (int)(baseNameColor.getRed()   + (rainbow.getRed()   - baseNameColor.getRed())   * ta),
            (int)(baseNameColor.getGreen() + (rainbow.getGreen() - baseNameColor.getGreen()) * ta),
            (int)(baseNameColor.getBlue()  + (rainbow.getBlue()  - baseNameColor.getBlue())  * ta));
        DrawHelper.drawTextBold(ctx, mod.name, x, y, onNameColor);

        // ── Glow под панелью (при включении) ─────────────────────────────
        if (ta > 0.02f) {
            DrawHelper.drawGlow(m, x, panelY, modW, PANEL_H, rad, 8, rainbowDim);
        }

        // ── Фон панели ────────────────────────────────────────────────────
        // Интерполируем от тёмного к чуть более тёмному при включении
        int bgR = (int)(28 + 8  * ta);
        int bgG = (int)(29 + 7  * ta);
        int bgB = (int)(38 + 16 * ta);
        DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, rad, new Color(bgR, bgG, bgB));

        // Лёгкий цветной overlay при включении
        if (ta > 0.01f) {
            DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, rad,
                new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), (int)(18 * ta)));
        }

        // Hover overlay
        if (ha > 0.01f) {
            DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, rad,
                new Color(255, 255, 255, (int)(10 * ha)));
        }

        // ── Линия-акцент сверху при включении ─────────────────────────────
        if (ta > 0.01f) {
            float lineW = (modW - rad * 2) * ta;
            DrawHelper.drawRect(m, x + rad, panelY, lineW, 2f, 0f, rainbowLine);
            // Glow под линией
            DrawHelper.drawRect(m, x + rad, panelY, lineW, 5f, 0f, rainbowGlow);
        }

        // ── Outline панели ────────────────────────────────────────────────
        DrawHelper.drawOutline(ms, x - 1, panelY - 1, modW + 2, PANEL_H + 2, rad,
            new Color(33, 32, 43), 2);

        // Цветной outline при hover/enable
        float outAlpha = Math.max(ha * 0.2f, ta * 0.45f);
        if (outAlpha > 0.01f) {
            DrawHelper.drawOutline(ms, x, panelY, modW, PANEL_H, rad,
                new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(),
                    (int)(255 * outAlpha)), 1);
        }

        // ── Статус текст ──────────────────────────────────────────────────
        float statusY = panelY + (PANEL_H - Fonts.height()) / 2f;
        Color statusC = mod.enabled
            ? new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(),
                        (int)(160 + 95 * ta))
            : new Color(80, 83, 120);
        DrawHelper.drawText(ctx, mod.enabled ? "Enabled" : "Disabled", x + 8, statusY, statusC);

        // ── Toggle ────────────────────────────────────────────────────────
        float tw  = 26f, th = 13f;
        float tx2 = x + modW - tw - 7;
        float ty2 = panelY + (PANEL_H - th) / 2f;

        // Фон тоггла: от тёмного к цветному
        Color toggleBg = new Color(
            (int)(21 + (rainbow.getRed()   - 21) * ta * 0.7f),
            (int)(22 + (rainbow.getGreen() - 22) * ta * 0.7f),
            (int)(29 + (rainbow.getBlue()  - 29) * ta * 0.7f));
        DrawHelper.drawRect(m, tx2, ty2, tw, th, th / 2f, toggleBg);
        DrawHelper.drawOutline(ms, tx2, ty2, tw, th, th / 2f, new Color(33, 32, 43), 1);

        // Knob
        float cs  = th - 4f;
        float kcx = tx2 + 2f + ta * (tw - cs - 4f);
        DrawHelper.drawCircle(m, kcx + cs/2f, ty2 + th/2f, 14, cs/2f,
            new Color(200, 205, 255));
    }

    public boolean mouseClicked(float x, float y, float modW,
                                Menu.ModuleEntry mod,
                                double mx, double my, int button) {
        float panelY = y + Fonts.height() + NAME_GAP;
        if (button == 0
         && mx >= x      && mx <= x + modW
         && my >= panelY && my <= panelY + PANEL_H) {
            mod.enabled = !mod.enabled;
            return true;
        }
        return false;
    }

    private static float lerp(float cur, float target, float speed) {
        return cur + (target - cur) * speed;
    }
}
