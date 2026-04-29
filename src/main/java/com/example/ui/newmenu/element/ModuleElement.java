package com.example.ui.menu.element;

import com.example.ui.menu.Menu.ModuleEntry;
import com.example.util.render.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ModuleElement {

    // Анимация включения для каждого модуля (lerp 0..1)
    private final Map<ModuleEntry, Float> enableAnim = new HashMap<>();

    // ─── Константы ───────────────────────────────────────────────────────────

    public static final int CARD_WIDTH  = 255;
    public static final int CARD_PADDING = 14;

    // ─── Рендер карточки ─────────────────────────────────────────────────────

    public void render(DrawContext context, int x, int y, ModuleEntry module) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // Обновляем анимацию
        float target = module.enabled ? 1f : 0f;
        float cur = enableAnim.getOrDefault(module, module.enabled ? 1f : 0f);
        float next = cur + (target - cur) * 0.15f;
        enableAnim.put(module, next);

        // ─── Цвета ───────────────────────────────────────────────────────────
        Color bgCard     = new Color(28, 29, 38);
        Color bgCardOn   = new Color(32, 34, 50);
        Color outlineOff = new Color(38, 39, 52);
        Color outlineOn  = new Color(75, 80, 140);
        Color nameOff    = new Color(127, 133, 172);
        Color nameOn     = new Color(197, 200, 255);

        int r, g, b;

        // Фон карточки (lerp)
        r = blend(bgCard.getRed(),   bgCardOn.getRed(),   next);
        g = blend(bgCard.getGreen(), bgCardOn.getGreen(), next);
        b = blend(bgCard.getBlue(),  bgCardOn.getBlue(),  next);
        Color bgColor = new Color(r, g, b);

        // Outline (lerp)
        r = blend(outlineOff.getRed(),   outlineOn.getRed(),   next);
        g = blend(outlineOff.getGreen(), outlineOn.getGreen(), next);
        b = blend(outlineOff.getBlue(),  outlineOn.getBlue(),  next);
        Color outColor = new Color(r, g, b);

        // ─── Название над карточкой ──────────────────────────────────────────
        r = blend(nameOff.getRed(),   nameOn.getRed(),   next);
        g = blend(nameOff.getGreen(), nameOn.getGreen(), next);
        b = blend(nameOff.getBlue(),  nameOn.getBlue(),  next);
        Color nameColor = new Color(r, g, b);

        context.drawText(tr, module.name, x, y, nameColor.getRGB(), false);

        // ─── Сама панель ─────────────────────────────────────────────────────
        int panelY = y + tr.fontHeight + 4;
        int panelH = getPanelHeight();

        // Фон
        DrawHelper.drawRect(context, x, panelY, CARD_WIDTH, panelH, bgColor);
        // Outline
        DrawHelper.drawOutlineRect(context, x - 2, panelY - 2, CARD_WIDTH + 4, panelH + 4, outColor, 2);

        // Акцентная полоска сверху при включённом
        if (next > 0.01f) {
            DrawHelper.drawAccentBar(context, x, panelY, (int)(CARD_WIDTH * next), 2);
        }

        // ─── Toggle строка ───────────────────────────────────────────────────
        int innerY = panelY + 10;

        // "Enabled" метка
        context.drawText(tr, "Enabled", x + 10, innerY, new Color(127, 133, 172).getRGB(), false);

        // Переключатель справа
        int toggleW = 30;
        int toggleH = 12;
        int toggleX = x + CARD_WIDTH - toggleW - 10;
        int toggleY = innerY - 2;

        Color toggleBg = new Color(
                blend(33, 51, next),
                blend(35, 56, next),
                blend(48, 94, next)
        );
        DrawHelper.drawRoundedRect(context, toggleX, toggleY, toggleW, toggleH, 5, toggleBg);

        int knobSize = 8;
        int knobX = (int)(toggleX + 2 + (toggleW - knobSize - 4) * next);
        int knobY = toggleY + 2;
        Color knobColor = new Color(
                blend(90, 125, next),
                blend(95, 136, next),
                blend(140, 255, next)
        );
        DrawHelper.drawRoundedRect(context, knobX, knobY, knobSize, knobSize, 4, knobColor);

        // Бинд
        String bind = module.bind > 0 ? keyName(module.bind) : "NONE";
        String bindText = "[" + bind + "]";
        int bindW = tr.getWidth(bindText);
        context.drawText(tr, bindText,
                x + CARD_WIDTH - bindW - 10,
                innerY + tr.fontHeight + 6,
                new Color(55, 58, 85).getRGB(), false);
    }

    // ─── Клик по карточке ────────────────────────────────────────────────────

    public boolean mouseClicked(int x, int y, ModuleEntry module, double mouseX, double mouseY, int button) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int panelY = y + tr.fontHeight + 4;
        int panelH = getPanelHeight();

        if (mouseX >= x && mouseX <= x + CARD_WIDTH &&
            mouseY >= panelY && mouseY <= panelY + panelH) {

            if (button == 0) { // ЛКМ — toggle
                module.enabled = !module.enabled;
                return true;
            }
            if (button == 1) { // ПКМ — задать бинд (заглушка)
                module.listeningForBind = !module.listeningForBind;
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(ModuleEntry module, int keyCode) {
        if (module.listeningForBind) {
            if (keyCode == 256) { // ESC — сброс
                module.bind = -1;
            } else {
                module.bind = keyCode;
            }
            module.listeningForBind = false;
            return true;
        }
        return false;
    }

    // ─── Размеры ─────────────────────────────────────────────────────────────

    private int getPanelHeight() {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        // Панель: padding 10 + toggle строка + bind строка + padding 10
        return 10 + tr.fontHeight + 6 + tr.fontHeight + 10;
    }

    public int getHeight() {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        return tr.fontHeight + 4 + getPanelHeight();
    }

    // ─── Хелперы ─────────────────────────────────────────────────────────────

    private int blend(int a, int b, float t) {
        return Math.min(255, Math.max(0, (int)(a + (b - a) * t)));
    }

    private String keyName(int keyCode) {
        return switch (keyCode) {
            case 256 -> "ESC";
            case 32  -> "SPACE";
            case 340 -> "LSHIFT";
            case 341 -> "LCTRL";
            default  -> keyCode >= 65 && keyCode <= 90
                    ? String.valueOf((char) keyCode)
                    : "KEY" + keyCode;
        };
    }
}
