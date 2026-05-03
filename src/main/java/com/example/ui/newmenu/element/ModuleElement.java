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

    public static final float BASE_H = 52f;

    public float getHeight() { return BASE_H; }

    public void render(DrawContext ctx, float x, float y, Menu.ModuleEntry mod, double mx, double my) {
        float w = getModuleWidth();
        float h = BASE_H;
        MatrixStack ms = ctx.getMatrices();

        boolean hovered = mx>=x && mx<=x+w && my>=y && my<=y+h;
        float ha = hoverAnims.getOrDefault(mod.name, 0f);
        ha += ((hovered?1f:0f)-ha)*0.15f;
        hoverAnims.put(mod.name, ha);

        float ta = toggleAnims.getOrDefault(mod.name, mod.enabled?1f:0f);
        ta += ((mod.enabled?1f:0f)-ta)*0.12f;
        toggleAnims.put(mod.name, ta);

        // Название модуля (как в оригинале — над панелью)
        Color nameColor = new Color(
            (int)(127+70*ta),
            (int)(133+67*ta),
            (int)(172+83*ta), 255);
        DrawHelper.drawTextShadow(ctx, mod.name, x, y, nameColor);

        float rectY = y + Fonts.height() + 4;
        float rectH = h - Fonts.height() - 4;

        // Панель модуля (как в оригинале)
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, rectY, w, rectH, 5, new Color(28, 29, 38));

        // Overlay если включён
        if (ta > 0.01f) {
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, rectY, w, rectH, 5,
                new Color(51, 56, 94, (int)(30*ta)));
        }

        // Outline (как в оригинале — x-2, y-2, w+4, h+4)
        DrawHelper.drawOutline(ms, x-2, rectY-2, w+4, rectH+4, 5,
            new Color(33, 32, 43), 2);

        // Акцент сверху если включён
        if (ta > 0.01f) {
            float lw = (w-10)*ta;
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x+5, rectY, lw, 2, 1,
                new Color(125, 136, 255, (int)(200*ta)));
        }

        // Статус
        String status = mod.enabled ? "Enabled" : "Disabled";
        Color statusC = mod.enabled
            ? new Color(100, 210, 130, (int)(180+75*ta))
            : new Color(127, 133, 172);
        DrawHelper.drawText(ctx, status,
            x+10, rectY+(rectH-Fonts.height())/2f, statusC);

        // Тоггл справа
        float tw=28f, th=14f;
        float tx=x+w-tw-8, ty=rectY+(rectH-th)/2f;
        Color tbg = new Color((int)(21+40*ta),(int)(22+45*ta),(int)(29+65*ta),255);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), tx, ty, tw, th, th/2f, tbg);
        DrawHelper.drawOutline(ms, tx, ty, tw, th, th/2f,
            new Color(33, 32, 43), 1);
        float cs=th-4f;
        float kcx=tx+2+ta*(tw-cs-4);
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), kcx, ty+2, cs, cs, cs/2f,
            new Color((int)(80+45*ta),(int)(90+46*ta),(int)(150+105*ta),255));

        // Hover outline
        if (ha > 0.02f)
            DrawHelper.drawOutline(ms, x, rectY, w, rectH, 5,
                new Color(70,80,200,(int)(40*ha)), 1);
    }

    public static float getModuleWidth() { return 255f; }

    public boolean mouseClicked(float x, float y, Menu.ModuleEntry mod, double mx, double my, int button) {
        float w = getModuleWidth();
        float rectY = y + Fonts.height() + 4;
        float rectH = BASE_H - Fonts.height() - 4;
        if (button==0 && mx>=x && mx<=x+w && my>=rectY && my<=rectY+rectH) {
            mod.enabled = !mod.enabled;
            return true;
        }
        return false;
    }
}
