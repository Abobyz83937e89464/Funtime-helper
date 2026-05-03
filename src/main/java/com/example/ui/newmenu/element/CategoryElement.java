package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryElement {

    public enum Category {
        COMBAT("Combat"), MOVEMENT("Move"), RENDER("Render"),
        PLAYER("Player"), WORLD("World"), MISC("Misc");
        private final String name;
        Category(String n) { this.name = n; }
        public String getName() { return name; }
    }

    private Category selected = Category.COMBAT;
    private final Map<Category, Float>   anims  = new LinkedHashMap<>();
    private final Map<Category, float[]> bounds = new LinkedHashMap<>();

    public CategoryElement() {
        for (Category c : Category.values())
            anims.put(c, c == selected ? 1f : 0f);
    }

    public Category getSelected() { return selected; }

    public boolean click(double mx, double my) {
        for (var e : bounds.entrySet()) {
            float[] b = e.getValue();
            if (mx>=b[0] && mx<=b[0]+b[2] && my>=b[1] && my<=b[1]+b[3]) {
                selected = e.getKey();
                return true;
            }
        }
        return false;
    }

    public void render(DrawContext ctx, float x, float y, float w, float h) {
        bounds.clear();
        Category[] cats = Category.values();
        float itemH = 22f, pad = 4f;
        float totalListH = cats.length * itemH + (cats.length-1) * pad;
        float startY = y + (h - totalListH) / 2f;

        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            float iy = startY + i*(itemH+pad);

            float anim = anims.getOrDefault(cat, 0f);
            anim += ((cat==selected ? 1f : 0f) - anim) * 0.15f;
            anims.put(cat, anim);

            float bx = x + 5f;
            float bw = w - 10f;
            float radius = itemH / 2f; // капсула — полностью круглые края

            // Фон кнопки
            Color bg = new Color(
                (int)(26 + 24*anim),
                (int)(28 + 27*anim),
                (int)(43 + 54*anim), 255);
            DrawHelper.drawRoundedRect(ctx, bx, iy, bw, itemH, radius, bg);

            // Gradient overlay при выборе
            if (anim > 0.02f) {
                DrawHelper.drawGradientH(ctx, bx, iy, bw * 0.5f, itemH,
                    new Color(60, 70, 180, (int)(30*anim)),
                    new Color(60, 70, 180, 0));
            }

            // Outline при выборе
            if (anim > 0.02f) {
                DrawHelper.drawOutline(ctx, bx, iy, bw, itemH, radius,
                    new Color(80, 100, 230, (int)(110*anim)));
            }

            // Акцентная точка слева
            if (anim > 0.02f) {
                float dotSize = 4f;
                DrawHelper.drawRoundedRect(ctx,
                    bx + 6, iy + (itemH - dotSize)/2f,
                    dotSize, dotSize, dotSize/2f,
                    new Color(110, 130, 255, (int)(255*anim)));
            }

            // Текст по центру кнопки
            float tx = bx + (bw - DrawHelper.twMed(cat.getName())) / 2f;
            float ty = iy + (itemH - DrawHelper.thMed()) / 2f;
            Color tc = new Color(
                (int)(115 + 120*anim),
                (int)(120 + 115*anim),
                (int)(165 + 78*anim), 255);
            DrawHelper.textMed(ctx, cat.getName(), tx, ty, tc);

            bounds.put(cat, new float[]{bx, iy, bw, itemH});
        }
    }
}
