package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ModuleElement {

    private final Map<String, Float> hoverAnim  = new HashMap<>();
    private final Map<String, Float> toggleAnim = new HashMap<>();

    public static final float CARD_H = 40f;

    public void render(DrawContext ctx, float x, float y, float w, Menu.ModuleEntry mod, double mx, double my) {
        boolean hovered = mx>=x && mx<=x+w && my>=y && my<=y+CARD_H;

        float ha = hoverAnim.getOrDefault(mod.name, 0f);
        ha += ((hovered ? 1f : 0f) - ha) * 0.18f;
        hoverAnim.put(mod.name, ha);

        float ta = toggleAnim.getOrDefault(mod.name, mod.enabled ? 1f : 0f);
        ta += ((mod.enabled ? 1f : 0f) - ta) * 0.14f;
        toggleAnim.put(mod.name, ta);

        // Фон карточки
        Color bg = new Color(
            (int)(24 + 8*ha + 5*ta),
            (int)(26 + 8*ha + 5*ta),
            (int)(38 + 8*ha + 10*ta), 255);
        DrawHelper.drawRoundedRect(ctx, x, y, w, CARD_H, 5, bg);

        // Верхняя линия если включён
        if (ta > 0.05f) {
            float lw = (w-10) * ta;
            DrawHelper.drawRoundedRect(ctx, x+5, y, lw, 1, 0,
                new Color(100, 120, 255, (int)(200*ta)));
        }

        // Outline
        Color ol = new Color(
            (int)(38 + 30*ta + 10*ha),
            (int)(40 + 35*ta + 10*ha),
            (int)(58 + 80*ta + 10*ha), 255);
        DrawHelper.drawOutline(ctx, x, y, w, CARD_H, 5, ol);

        // Название модуля
        Color nameC = new Color(
            (int)(170 + 85*ta),
            (int)(175 + 80*ta),
            (int)(210 + 45*ta), 255);
        DrawHelper.drawTextShadow(ctx, mod.name, x+8, y+7, nameC);

        // Статус
        String status = mod.enabled ? "ON" : "OFF";
        Color statusC = mod.enabled
            ? new Color(100, 220, 130, (int)(200+55*ta))
            : new Color(80, 83, 110);
        DrawHelper.drawText(ctx, status, x+8, y+7+DrawHelper.textHeight()+3, statusC);

        // Тоггл-переключатель
        float tw = 22f, th = 11f;
        float tx = x+w-tw-6, ty2 = y+(CARD_H-th)/2f;
        Color tbg = new Color((int)(20+40*ta), (int)(20+45*ta), (int)(30+90*ta), 255);
        DrawHelper.drawRoundedRect(ctx, tx, ty2, tw, th, th/2f, tbg);
        DrawHelper.drawOutline(ctx, tx, ty2, tw, th, th/2f, ol);
        float cs = th-3;
        float kcx = tx+1.5f + ta*(tw-cs-3);
        DrawHelper.drawRoundedRect(ctx, kcx, ty2+1.5f, cs, cs, cs/2f,
            new Color((int)(90+60*ta),(int)(100+60*ta),(int)(160+95*ta), 255));
    }
}
