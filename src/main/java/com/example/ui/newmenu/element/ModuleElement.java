package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ModuleElement {

    private final Map<String, Float> hoverAnims  = new HashMap<>();
    private final Map<String, Float> toggleAnims = new HashMap<>();

    public static final float BASE_H = 40f;

    public float getHeight() { return BASE_H; }

    public static float getModuleWidth() { return 96f; }

    public void render(DrawContext ctx, float x, float y, Menu.ModuleEntry mod, double mx, double my) {
        float w = getModuleWidth();
        MatrixStack ms = ctx.getMatrices();

        // Hover анимация
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + BASE_H;
        float ha = hoverAnims.getOrDefault(mod.name, 0f);
        ha += ((hovered ? 1f : 0f) - ha) * 0.14f;
        hoverAnims.put(mod.name, ha);

        // Toggle анимация
        float ta = toggleAnims.getOrDefault(mod.name, mod.enabled ? 1f : 0f);
        ta += ((mod.enabled ? 1f : 0f) - ta) * 0.12f;
        toggleAnims.put(mod.name, ta);

        float radius = 6f;

        // Фон карточки модуля
        Color bgBase  = new Color(22, 23, 32);
        Color bgActive = new Color(28, 30, 45);
        int bgR = (int)(bgBase.getRed()   + (bgActive.getRed()   - bgBase.getRed())   * ta);
        int bgG = (int)(bgBase.getGreen() + (bgActive.getGreen() - bgBase.getGreen()) * ta);
        int bgB = (int)(bgBase.getBlue()  + (bgActive.getBlue()  - bgBase.getBlue())  * ta);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, y, w, BASE_H, radius, new Color(bgR, bgG, bgB));

        // Hover overlay
        if (ha > 0.01f)
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, y, w, BASE_H, radius,
                new Color(255, 255, 255, (int)(8 * ha)));

        // Outline карточки
        Color outlineCol = new Color(
            (int)(38 + 30 * ta),
            (int)(37 + 35 * ta),
            (int)(52 + 50 * ta));
        DrawHelper.drawOutline(ms, x - 1, y - 1, w + 2, BASE_H + 2, radius,
            outlineCol, 1);

        // Акцент сверху если включён (фиолетовая линия)
        if (ta > 0.01f) {
            float lw = (w - radius * 2) * ta;
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + radius, y, lw, 2, 1,
                new Color(110, 120, 255, (int)(210 * ta)));
            // Glow под линией
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + radius, y + 2, lw, 3, 0,
                new Color(100, 110, 255, (int)(40 * ta)));
        }

        // Название модуля (Inter Bold)
        Color nameColor = new Color(
            (int)(150 + 47 * ta),
            (int)(155 + 45 * ta),
            (int)(195 + 60 * ta));
        DrawHelper.drawText(ctx, mod.name, x + 6, y + 6, nameColor);

        // Тоггл переключатель
        float tw = 22f, th = 11f;
        float tx = x + w - tw - 5;
        float ty = y + BASE_H - th - 6;

        Color tbg = new Color(
            (int)(18 + 35 * ta),
            (int)(20 + 38 * ta),
            (int)(28 + 60 * ta));
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), tx, ty, tw, th, th / 2f, tbg);
        DrawHelper.drawOutline(ms, tx - 1, ty - 1, tw + 2, th + 2, th / 2f + 1,
            new Color(38, 37, 52), 1);

        float cs = th - 4f;
        float kx  = tx + 2 + ta * (tw - cs - 4);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), kx, ty + 2, cs, cs, cs / 2f,
            new Color((int)(70 + 55 * ta), (int)(80 + 50 * ta), (int)(140 + 115 * ta)));

        // Статус текст
        String status = mod.enabled ? "ON" : "OFF";
        Color statusC = mod.enabled
            ? new Color(100, 200, 130, (int)(160 + 95 * ta))
            : new Color(90, 90, 120);
        DrawHelper.drawTextMedium(ctx, status, x + 6, ty + (th - Fonts.height()) / 2f, statusC);
    }

    public boolean mouseClicked(float x, float y, Menu.ModuleEntry mod, double mx, double my, int button) {
        float w = getModuleWidth();
        if (button == 0 && mx >= x && mx <= x + w && my >= y && my <= y + BASE_H) {
            mod.enabled = !mod.enabled;
            return true;
        }
        return false;
    }
}
