package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ModuleElement {

    private final Map<String, Float> hoverAnim  = new HashMap<>();
    private final Map<String, Float> toggleAnim = new HashMap<>();

    public void render(DrawContext ctx, float x, float y, float w, Menu.ModuleEntry module, double mx, double my) {
        float h = getHeight();
        float radius = 8f;

        boolean hovered = mx >= x && mx <= x+w && my >= y && my <= y+h;

        float ha = hoverAnim.getOrDefault(module.name, 0f);
        ha += ((hovered ? 1f : 0f) - ha) * 0.15f;
        hoverAnim.put(module.name, ha);

        float ta = toggleAnim.getOrDefault(module.name, module.enabled ? 1f : 0f);
        ta += ((module.enabled ? 1f : 0f) - ta) * 0.12f;
        toggleAnim.put(module.name, ta);

        // === Фон ===
        Color bg = new Color(
            (int)(22 + ha*10 + ta*6),
            (int)(23 + ha*10 + ta*6),
            (int)(32 + ha*10 + ta*12), 255);
        DrawHelper.drawRoundedRect(ctx, x, y, w, h, radius, bg);

        // === Gradient overlay (сверху) ===
        if (ta > 0.01f) {
            DrawHelper.drawGradientRect(ctx, x, y, w, h * 0.45f,
                new Color(51, 56, 94, (int)(55 * ta)),
                new Color(51, 56, 94, 0));
        }

        // === Outline ===
        Color ol = new Color(
            (int)(33 + ta*42 + ha*10),
            (int)(32 + ta*48 + ha*10),
            (int)(43 + ta*97 + ha*10), 255);
        DrawHelper.drawRoundedOutline(ctx, x, y, w, h, radius, ol, 1);

        // === Акцент сверху (тонкая линия) ===
        if (ta > 0.01f) {
            DrawHelper.drawRoundedRect(ctx,
                x + radius, y, w - radius*2, 2, 1,
                new Color(125, 136, 255, (int)(255 * ta)));
        }

        // === Название ===
        Color nameC = new Color(
            (int)(127 + ta*70),
            (int)(133 + ta*67),
            (int)(172 + ta*83), 255);
        DrawHelper.drawTextShadow(ctx, Fonts.BOLD, module.name, x + 12, y + 10, nameC);

        // === Статус ===
        String status = module.enabled ? "enabled" : "disabled";
        Color statusC = module.enabled
            ? new Color(100, 210, 130, (int)(180 + ta*75))
            : new Color(70, 73, 100);
        DrawHelper.drawText(ctx, Fonts.REGULAR, status, x + 12, y + 10 + Fonts.BOLD.fontHeight + 4, statusC);

        // === Toggle кнопка ===
        float tw = 30f, th = 15f;
        float tx = x + w - tw - 10;
        float ty = y + (h - th) / 2f;

        Color tbg = new Color(
            (int)(21 + ta*30),
            (int)(22 + ta*34),
            (int)(29 + ta*65), 255);
        DrawHelper.drawRoundedRect(ctx, tx, ty, tw, th, th/2f, tbg);
        DrawHelper.drawRoundedOutline(ctx, tx, ty, tw, th, th/2f, ol, 1);

        // Кружок
        float cs = th - 4;
        float cx2 = tx + 2 + ta * (tw - cs - 4);
        float cy2 = ty + 2;
        Color cc = new Color(
            (int)(80 + ta*45),
            (int)(90 + ta*46),
            (int)(150 + ta*105), 255);
        DrawHelper.drawRoundedRect(ctx, cx2, cy2, cs, cs, cs/2f, cc);
    }

    public float getHeight() { return 52f; }
}
