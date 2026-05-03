package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ModuleElement {

    public static final float H = 40f;
    private final Map<String, Float> hov = new HashMap<>();
    private final Map<String, Float> tog = new HashMap<>();

    public void render(DrawContext ctx, float x, float y, float w, Menu.ModuleEntry mod, double mx, double my) {
        boolean hovered = mx>=x&&mx<=x+w&&my>=y&&my<=y+H;

        float ha = hov.getOrDefault(mod.name, 0f);
        ha += ((hovered?1f:0f)-ha)*0.18f;
        hov.put(mod.name, ha);

        float ta = tog.getOrDefault(mod.name, mod.enabled?1f:0f);
        ta += ((mod.enabled?1f:0f)-ta)*0.14f;
        tog.put(mod.name, ta);

        // Фон
        Color bg = new Color((int)(22+7*ha+4*ta),(int)(24+7*ha+4*ta),(int)(36+7*ha+9*ta),255);
        DrawHelper.drawRoundedRect(ctx, x, y, w, H, 6, bg);

        // Полоска сверху если включён
        if (ta > 0.05f) {
            float lw = (w-8)*ta;
            DrawHelper.drawRoundedRect(ctx, x+4, y, lw, 1, 0, new Color(95,115,255,(int)(200*ta)));
        }

        // Outline
        Color ol = new Color((int)(35+28*ta+7*ha),(int)(37+33*ta+7*ha),(int)(53+80*ta+7*ha),255);
        DrawHelper.drawOutline(ctx, x, y, w, H, 6, ol);

        // Название модуля — крупный шрифт
        Color nc = new Color((int)(160+80*ta),(int)(165+75*ta),(int)(200+48*ta),255);
        DrawHelper.textBig(ctx, mod.name, x+7, y+5, nc);

        // Статус — мелкий
        Color sc = mod.enabled ? new Color(90,205,120,(int)(185+70*ta)) : new Color(72,75,108);
        DrawHelper.text(ctx, mod.enabled ? "ON" : "OFF", x+8, y+5+DrawHelper.thBig()+2, sc);

        // Тоггл
        float tw=20f, th=10f;
        float tx=x+w-tw-5, ty=y+(H-th)/2f;
        DrawHelper.drawRoundedRect(ctx, tx, ty, tw, th, th/2f,
            new Color((int)(17+36*ta),(int)(17+40*ta),(int)(25+86*ta),255));
        DrawHelper.drawOutline(ctx, tx, ty, tw, th, th/2f, ol);
        float cs=th-3, kcx=tx+1.5f+ta*(tw-cs-3);
        DrawHelper.drawRoundedRect(ctx, kcx, ty+1.5f, cs, cs, cs/2f,
            new Color((int)(82+56*ta),(int)(92+55*ta),(int)(152+90*ta),255));
    }
}
