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

    private final Map<String, Float>   hoverAnims   = new HashMap<>();
    private final Map<String, Float>   toggleAnims  = new HashMap<>();
    private final Map<String, Integer> moduleIndex  = new HashMap<>();
    private int indexCounter = 0;

    public float getHeight() {
        return Fonts.height() + NAME_GAP + PANEL_H;
    }

    public void render(DrawContext ctx, MatrixStack ms,
                       float x, float y, float modW,
                       Menu.ModuleEntry mod,
                       double mx, double my) {

        Matrix4f m = ms.peek().getPositionMatrix();

        // Индекс модуля для сдвига радуги
        moduleIndex.computeIfAbsent(mod.name, k -> indexCounter++);
        int idx = moduleIndex.getOrDefault(mod.name, 0);

        // Hover / toggle анимации
        boolean hovered = mx >= x && mx <= x + modW
                       && my >= y && my <= y + getHeight();
        float ha = lerp(hoverAnims.getOrDefault(mod.name,  0f),
                        hovered     ? 1f : 0f, 0.16f);
        float ta = lerp(toggleAnims.getOrDefault(mod.name, 0f),
                        mod.enabled ? 1f : 0f, 0.13f);
        hoverAnims.put(mod.name, ha);
        toggleAnims.put(mod.name, ta);

        float panelY = y + Fonts.height() + NAME_GAP;
        float rad    = 6f;

        // Rainbow цвет с индивидуальным сдвигом
        Color rainbow = DrawHelper.getRainbow(idx * 0.065f, 4f, 0.55f, 1f, 255);

        Color rainbowDim  = new Color(
            rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(),
            (int)(80 * ta));
        Color rainbowLine = new Color(
            rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(),
            (int)(220 * ta));
        Color rainbowGlow = new Color(
            rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(),
            (int)(30 * ta));

        // ── Название ──────────────────────────────────────────────
        Color baseNameC = new Color(100, 103, 145);
        Color nameColor = new Color(
            (int)(baseNameC.getRed()   + (rainbow.getRed()   - baseNameC.getRed())   * ta),
            (int)(baseNameC.getGreen() + (rainbow.getGreen() - baseNameC.getGreen()) * ta),
            (int)(baseNameC.getBlue()  + (rainbow.getBlue()  - baseNameC.getBlue())  * ta));
        DrawHelper.drawTextBold(ctx, mod.name, x, y, nameColor);

        // ── Glow под панелью при включении ────────────────────────
        if (ta > 0.02f) {
            DrawHelper.drawGlow(m, x, panelY, modW, PANEL_H, rad, 8, rainbowDim);
        }

        // ── Фон панели ────────────────────────────────────────────
        int bgR = (int)(28 + 8  * ta);
        int bgG = (int)(29 + 7  * ta);
        int bgB = (int)(38 + 16 * ta);
        DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, rad,
            new Color(clamp(bgR), clamp(bgG), clamp(bgB)));

        // Цветной overlay при включении
        if (ta > 0.01f) {
            DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, rad,
                new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(),
                    (int)(18 * ta)));
        }

        // Hover overlay
        if (ha > 0.01f) {
            DrawHelper.drawRect(m, x, panelY, modW, PANEL_H, rad,
                new Color(255, 255, 255, (int)(10 * ha)));
        }

        // ── Линия-акцент сверху при включении ────────────────────
        if (ta > 0.01f) {
            float lineW = (modW - rad * 2f) * ta;
            DrawHelper.drawRect(m, x + rad, panelY, lineW, 2f, 0f, rainbowLine);
            DrawHelper.drawRect(m, x + rad, panelY, lineW, 5f, 0f, rainbowGlow);
        }

        // ── Outline панели ────────────────────────────────────────
        DrawHelper.drawOutline(ms, x - 1, panelY - 1, modW + 2, PANEL_H + 2, rad,
            new Color(33, 32, 43), 2f);

        // Цветной outline при hover/enable
        float outAlpha = Math.max(ha * 0.2f, ta * 0.45f);
        if (outAlpha > 0.01f) {
            DrawHelper.drawOutline(ms, x, panelY, modW, PANEL_H, rad,
                new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(),
                    (int)(255 * outAlpha)), 1f);
        }

        // ── Статус текст ──────────────────────────────────────────
        float statusY = panelY + (PANEL_H - Fonts.height()) / 2f;
        Color statusC = mod.enabled
            ? new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(),
                        (int)(160 + 95 * ta))
            : new Color(80, 83, 120);
        DrawHelper.drawText(ctx, mod.enabled ? "Enabled" : "Disabled",
            x + 8, statusY, statusC);

        // ── Toggle ────────────────────────────────────────────────
        float tw  = 26f;
        float th  = 13f;
        float tx2 = x + modW - tw - 7f;
        float ty2 = panelY + (PANEL_H - th) / 2f;

        // Фон тоггла
        Color toggleBg = new Color(
            clamp((int)(21 + (rainbow.getRed()   - 21) * ta * 0.7f)),
            clamp((int)(22 + (rainbow.getGreen() - 22) * ta * 0.7f)),
            clamp((int)(29 + (rainbow.getBlue()  - 29) * ta * 0.7f)));
        DrawHelper.drawRect(m, tx2, ty2, tw, th, th / 2f, toggleBg);

        // Outline тоггла
        DrawHelper.drawOutline(ms, tx2, ty2, tw, th, th / 2f,
            new Color(33, 32, 43), 1f);

        // Кружок тоггла — плавно едет вправо
        float cs  = th - 4f;
        float kcx = tx2 + 2f + ta * (tw - cs - 4f);
        DrawHelper.drawCircle(m, kcx + cs / 2f, ty2 + th / 2f, 14, cs / 2f,
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

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
