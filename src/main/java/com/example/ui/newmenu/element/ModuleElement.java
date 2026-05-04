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

    // Высота карточки модуля (имя + панель)
    private static final float NAME_GAP  = 3f;   // отступ между именем и панелью
    private static final float PANEL_H   = 32f;  // высота самой панели
    public  static final float BASE_H    = Fonts.height() + NAME_GAP + PANEL_H;

    // Анимации: hover (0→1) и toggle (0→1)
    private final Map<String, Float> hoverAnims  = new HashMap<>();
    private final Map<String, Float> toggleAnims = new HashMap<>();

    // ── Высоты ───────────────────────────────────────────────────────────

    public float getHeight() {
        return Fonts.height() + NAME_GAP + PANEL_H;
    }

    // ── Render ────────────────────────────────────────────────────────────

    /**
     * @param modW — ширина карточки, вычисляется в Menu динамически
     */
    public void render(DrawContext ctx, MatrixStack ms,
                       float x, float y, float modW,
                       Menu.ModuleEntry mod, double mx, double my) {
        Matrix4f m = ms.peek().getPositionMatrix();

        // Анимации
        boolean hovered = mx >= x && mx <= x + modW && my >= y && my <= y + getHeight();
        float ha = lerp(hoverAnims.getOrDefault(mod.name,  0f), hovered      ? 1f : 0f, 0.16f);
        float ta = lerp(toggleAnims.getOrDefault(mod.name, mod.enabled?1f:0f), mod.enabled ? 1f : 0f, 0.14f);
        hoverAnims.put(mod.name, ha);
        toggleAnims.put(mod.name, ta);

        float panelY = y + Fonts.height() + NAME_GAP;

        // ── Название модуля (над панелью) ────────────────────────────────
        // Цвет: серый → бело-синий при включении
        Color nameColor = new Color(
            (int)(100 + 97 * ta),
            (int)(103 + 97 * ta),
            (int)(145 + 110 * ta), 255);
        DrawHelper.drawText(ctx, mod.name, x, y, nameColor);

        // ── Glow под панелью если включён ────────────────────────────────
        if (ta > 0.02f) {
            DrawHelper.drawGlow(m, x, panelY, modW, PANEL_H, 5, 7,
                new Color(125, 136, 255, (int)(28 * ta)));
        }

        // ── Панель (фон) ─────────────────────────────────────────────────
        DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, 5,
            new Color(28, 29, 38));

        // Синий overlay при включении
        if (ta > 0.01f) {
            DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, 5,
                new Color(51, 56, 94, (int)(28 * ta)));
        }

        // Hover overlay
        if (ha > 0.01f) {
            DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, 5,
                new Color(255, 255, 255, (int)(8 * ha)));
        }

        // ── Линия-акцент сверху при включении ────────────────────────────
        if (ta > 0.01f) {
            float lineW = (modW - 10f) * ta;
            DrawHelper.drawRect(m, x + 5f, panelY, lineW, 2f, 1f,
                new Color(125, 136, 255, (int)(210 * ta)));
        }

        // ── Outline панели ────────────────────────────────────────────────
        DrawHelper.drawOutline(ms, x - 1, panelY - 1, modW + 2, PANEL_H + 2, 5,
            new Color(33, 32, 43), 2);

        // Дополнительный цветной outline при hover/enabled
        float outlineAlpha = Math.max(ha * 0.25f, ta * 0.35f);
        if (outlineAlpha > 0.01f) {
            DrawHelper.drawOutline(ms, x, panelY, modW, PANEL_H, 5,
                new Color(100, 110, 220, (int)(255 * outlineAlpha)), 1);
        }

        // ── Статус (Enabled / Disabled) ───────────────────────────────────
        String status  = mod.enabled ? "Enabled" : "Disabled";
        Color statusC  = mod.enabled
            ? new Color(100, 210, 130, (int)(160 + 95 * ta))
            : new Color(90, 93, 130);
        float statusY  = panelY + (PANEL_H - Fonts.height()) / 2f;
        DrawHelper.drawText(ctx, status, x + 8, statusY, statusC);

        // ── Toggle-кнопка справа ─────────────────────────────────────────
        float tw  = 26f, th = 13f;
        float tx2 = x + modW - tw - 7;
        float ty2 = panelY + (PANEL_H - th) / 2f;

        // Фон тоггла
        Color tbg = new Color(
            (int)(21 + 35 * ta),
            (int)(22 + 40 * ta),
            (int)(29 + 60 * ta), 255);
        DrawHelper.drawRect(m, tx2, ty2, tw, th, th / 2f, tbg);

        // Outline тоггла
        DrawHelper.drawOutline(new MatrixStack() {{ peek().getPositionMatrix().set(m); }},
            tx2, ty2, tw, th, th / 2f, new Color(33, 32, 43), 1);

        // Кружок
        float cs  = th - 4f;
        float kcx = tx2 + 2f + ta * (tw - cs - 4f);
        DrawHelper.drawCircle(m, kcx + cs / 2f, ty2 + th / 2f, 12,
            cs / 2f,
            new Color((int)(75 + 50 * ta), (int)(85 + 51 * ta), (int)(145 + 110 * ta)));
    }

    // ── Mouse ─────────────────────────────────────────────────────────────

    public boolean mouseClicked(float x, float y, float modW,
                                Menu.ModuleEntry mod,
                                double mx, double my, int button) {
        float panelY = y + Fonts.height() + NAME_GAP;
        if (button == 0
         && mx >= x    && mx <= x + modW
         && my >= panelY && my <= panelY + PANEL_H) {
            mod.enabled = !mod.enabled;
            return true;
        }
        return false;
    }

    // ── Util ──────────────────────────────────────────────────────────────

    private static float lerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
}
