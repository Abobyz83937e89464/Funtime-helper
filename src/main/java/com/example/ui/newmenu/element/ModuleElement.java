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

    // Меньше чем было (52 -> 44)
    public static final float BASE_H = 44f;

    public float getHeight() { return BASE_H; }

    public static float getModuleWidth() { return 100f; }

    public void render(DrawContext ctx, float x, float y, Menu.ModuleEntry mod, double mx, double my) {
        float w = getModuleWidth();
        MatrixStack ms = ctx.getMatrices();

        // Hover анимация
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + BASE_H;
        float ha = hoverAnims.getOrDefault(mod.name, 0f);
        ha += ((hovered ? 1f : 0f) - ha) * 0.15f;
        hoverAnims.put(mod.name, ha);

        // Toggle анимация
        float ta = toggleAnims.getOrDefault(mod.name, mod.enabled ? 1f : 0f);
        ta += ((mod.enabled ? 1f : 0f) - ta) * 0.12f;
        toggleAnims.put(mod.name, ta);

        // Название модуля (Inter Bold, над панелью)
        Color nameColor = new Color(
            (int)(127 + 70 * ta),
            (int)(133 + 67 * ta),
            (int)(172 + 83 * ta), 255);
        DrawHelper.drawText(ctx, mod.name, x, y, nameColor);

        float rectY = y + Fonts.height() + 3;
        float rectH = BASE_H - Fonts.height() - 3;

        // Фон панели
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, rectY, w, rectH, 5,
            new Color(28, 29, 38));

        // Цветной оверлей если включён
        if (ta > 0.01f) {
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, rectY, w, rectH, 5,
                new Color(51, 56, 94, (int)(25 * ta)));
        }

        // Outline панели
        DrawHelper.drawOutline(ms, x - 1, rectY - 1, w + 2, rectH + 2, 5,
            new Color(33, 32, 43), 2);

        // Акцент сверху если включён (фиолетовая линия)
        if (ta > 0.01f) {
            float lw = (w - 10) * ta;
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + 5, rectY, lw, 2, 1,
                new Color(125, 136, 255, (int)(200 * ta)));
        }

        // Тоггл (переключатель справа, в центре по вертикали)
        float tw = 24f, th = 12f;
        float tx = x + w - tw - 6;
        float ty = rectY + (rectH - th) / 2f;
        Color tbg = new Color(
            (int)(21 + 40 * ta),
            (int)(22 + 45 * ta),
            (int)(29 + 65 * ta), 255);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), tx, ty, tw, th, th / 2f, tbg);
        DrawHelper.drawOutline(ms, tx, ty, tw, th, th / 2f, new Color(33, 32, 43), 1);

        float cs = th - 4f;
        float kx  = tx + 2 + ta * (tw - cs - 4);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), kx, ty + 2, cs, cs, cs / 2f,
            new Color((int)(80 + 45 * ta), (int)(90 + 46 * ta), (int)(150 + 105 * ta), 255));

        // Hover outline
        if (ha > 0.02f)
            DrawHelper.drawOutline(ms, x, rectY, w, rectH, 5,
                new Color(70, 80, 200, (int)(40 * ha)), 1);
    }

    public boolean mouseClicked(float x, float y, Menu.ModuleEntry mod, double mx, double my, int button) {
        float w    = getModuleWidth();
        float rectY = y + Fonts.height() + 3;
        float rectH = BASE_H - Fonts.height() - 3;
        if (button == 0 && mx >= x && mx <= x + w && my >= rectY && my <= rectY + rectH) {
            mod.enabled = !mod.enabled;
            return true;
        }
        return false;
    }
}
