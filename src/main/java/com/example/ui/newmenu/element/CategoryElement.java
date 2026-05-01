package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryElement {

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        RENDER("Render"),
        PLAYER("Player"),
        WORLD("World"),
        MISC("Misc");

        private final String displayName;
        Category(String d) { this.displayName = d; }
        public String getDisplayName() { return displayName; }
    }

    private Category selectedCategory = Category.COMBAT;
    private final Map<Category, float[]> bounds = new LinkedHashMap<>();
    private final Map<Category, Float> selAnim  = new LinkedHashMap<>();

    public CategoryElement() {
        for (Category c : Category.values())
            selAnim.put(c, c == selectedCategory ? 1f : 0f);
    }

    public Category getSelectedCategory() { return selectedCategory; }

    public boolean mouseClicked(double mx, double my) {
        for (var e : bounds.entrySet()) {
            float[] b = e.getValue();
            if (mx >= b[0] && mx <= b[0]+b[2] && my >= b[1] && my <= b[1]+b[3]) {
                selectedCategory = e.getKey();
                return true;
            }
        }
        return false;
    }

    public void render(DrawContext ctx, float fx, float fy, float fw, float fh) {
        Category[] cats = Category.values();
        float spacing = 6f;
        float btnH = 24f;
        float radius = 6f;

        float totalW = 0;
        for (Category c : cats)
            totalW += Fonts.MEDIUM.getWidth(c.getDisplayName()) + 24 + spacing;
        totalW -= spacing;

        float curX = fx + (fw - totalW) / 2f;
        float btnY  = fy + (fh - btnH) / 2f;

        bounds.clear();

        for (Category cat : cats) {
            float anim = selAnim.getOrDefault(cat, 0f);
            float target = cat == selectedCategory ? 1f : 0f;
            anim += (target - anim) * 0.15f;
            selAnim.put(cat, anim);

            float btnW = Fonts.MEDIUM.getWidth(cat.getDisplayName()) + 24;

            // Фон
            Color bg = new Color(
                (int)(28 + 23 * anim),
                (int)(29 + 27 * anim),
                (int)(38 + 56 * anim), 255);
            DrawHelper.drawRoundedRect(ctx, curX, btnY, btnW, btnH, radius, bg);

            // Outline
            if (anim > 0.02f) {
                Color ol = new Color(75, 80, 140, (int)(255 * anim));
                DrawHelper.drawRoundedOutline(ctx, curX, btnY, btnW, btnH, radius, ol, 1);
            }

            // Акцентная линия снизу
            if (anim > 0.02f) {
                float lw = btnW * 0.6f * anim;
                DrawHelper.drawRoundedRect(ctx,
                    curX + (btnW - lw) / 2f,
                    btnY + btnH - 2,
                    lw, 2, 1,
                    new Color(125, 136, 255, (int)(255 * anim)));
            }

            // Текст
            Color tc = new Color(
                (int)(110 + 87 * anim),
                (int)(114 + 86 * anim),
                (int)(160 + 95 * anim), 255);
            float tx = curX + (btnW - Fonts.MEDIUM.getWidth(cat.getDisplayName())) / 2f;
            float ty = btnY + (btnH - Fonts.MEDIUM.fontHeight) / 2f;
            DrawHelper.drawText(ctx, Fonts.MEDIUM, cat.getDisplayName(), tx, ty, tc);

            bounds.put(cat, new float[]{curX, btnY, btnW, btnH});
            curX += btnW + spacing;
        }
    }
}
