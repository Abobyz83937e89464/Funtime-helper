package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ModuleElement {

    private static final float NAME_GAP = 3f;
    private static final float PANEL_H  = 34f;

    private final Map<String, Float>   hoverAnims  = new HashMap<>();
    private final Map<String, Float>   toggleAnims = new HashMap<>();
    private final Map<String, Integer> moduleIndex = new HashMap<>();
    private int indexCounter = 0;

    public float getHeight() { return Fonts.height() + NAME_GAP + PANEL_H; }

    public void render(DrawContext ctx, MatrixStack ms,
                       float x, float y, float modW,
                       Menu.ModuleEntry mod, double mx, double my) {
        Matrix4f m = ms.peek().getPositionMatrix();

        moduleIndex.computeIfAbsent(mod.name, k -> indexCounter++);
        int   idx       = moduleIndex.getOrDefault(mod.name, 0);
        float idxOffset = idx * 0.065f;

        boolean hovered = mx >= x && mx <= x + modW && my >= y && my <= y + getHeight();
        float   ha = lerp(hoverAnims.getOrDefault(mod.name,  0f), hovered     ? 1f : 0f, 0.16f);
        float   ta = lerp(toggleAnims.getOrDefault(mod.name, 0f), mod.enabled ? 1f : 0f, 0.13f);
        hoverAnims.put(mod.name, ha);
        toggleAnims.put(mod.name, ta);

        float panelY = y + Fonts.height() + NAME_GAP;
        float rad    = 6f;

        Color rainbow = DrawHelper.getRainbow(idxOffset, 4f, 0.55f, 1f, 255);

        // ── 1. Карточка ───────────────────────────────────────────
        DrawHelper.drawModuleCard(m, x, panelY, modW, PANEL_H, rad,
                                  idxOffset, ta, ha, rainbow);

        // ── 2. Toggle ─────────────────────────────────────────────
        float tw  = 26f, th = 13f;
        float tx2 = x + modW - tw - 7;
        float ty2 = panelY + (PANEL_H - th) / 2f;

        Color toggleBg = new Color(
            clamp((int)(21 + (rainbow.getRed()   - 21) * ta * 0.7f)),
            clamp((int)(22 + (rainbow.getGreen() - 22) * ta * 0.7f)),
            clamp((int)(29 + (rainbow.getBlue()  - 29) * ta * 0.7f)));

        DrawHelper.drawRect(m, tx2, ty2, tw, th, th / 2f, toggleBg);
        DrawHelper.drawOutline(ms, tx2, ty2, tw, th, th / 2f,
            new Color(33, 32, 43), 1);

        float cs  = th - 4f;
        float kcx = tx2 + 2f + ta * (tw - cs - 4f);
        DrawHelper.drawCircle(m, kcx + cs / 2f, ty2 + th / 2f, 14, cs / 2f,
            new Color(200, 205, 255));

        // ── 3. ТЕКСТ — строго последним + восстанавливаем blend ───
        // ✅ ФИКС: после сырых GL шейдеров restore() отключает blend →
        //          ctx.drawText() без blend = текст невидим
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Имя модуля (над карточкой)
        Color baseNameC = new Color(100, 103, 145);
        Color nameColor = new Color(
            clamp((int)(baseNameC.getRed()   + (rainbow.getRed()   - baseNameC.getRed())   * ta)),
            clamp((int)(baseNameC.getGreen() + (rainbow.getGreen() - baseNameC.getGreen()) * ta)),
            clamp((int)(baseNameC.getBlue()  + (rainbow.getBlue()  - baseNameC.getBlue())  * ta)));
        DrawHelper.drawTextBold(ctx, mod.name, x, y, nameColor);

        // Статус внутри карточки
        float statusY = panelY + (PANEL_H - Fonts.height()) / 2f;
        Color statusC = mod.enabled
            ? new Color(
                rainbow.getRed(),
                rainbow.getGreen(),
                rainbow.getBlue(),
                clamp((int)(160 + 95 * ta)))
            : new Color(80, 83, 120);
        DrawHelper.drawText(ctx, mod.enabled ? "Enabled" : "Disabled",
                            x + 8, statusY, statusC);

        RenderSystem.disableBlend();
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
