package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
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

        private final String name;
        Category(String n) { this.name = n; }
        public String getName() { return name; }
    }

    private Category selected = Category.COMBAT;
    private final Map<Category, Float> anims = new LinkedHashMap<>();
    private final Map<Category, float[]> bounds = new LinkedHashMap<>();

    public CategoryElement() {
        for (Category c : Category.values())
            anims.put(c, c == selected ? 1f : 0f);
    }

    public Category getSelected() { return selected; }

    public boolean click(double mx, double my) {
        for (var e : bounds.entrySet()) {
            float[] b = e.getValue();
            if (mx >= b[0] && mx <= b[0]+b[2] && my >= b[1] && my <= b[1]+b[3]) {
                selected = e.getKey();
                return true;
            }
        }
        return false;
    }

    // Рендер вертикального списка категорий (sidebar)
    public void render(DrawContext ctx, float x, float y, float w, float h) {
        bounds.clear();
        Category[] cats = Category.values();
        float itemH = 22f, pad = 4f;
        float startY = y + (h - (cats.length * itemH + (cats.length-1)*pad)) / 2f;

        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            float iy = startY + i*(itemH+pad);

            float anim = anims.getOrDefault(cat, 0f);
            anim += ((cat == selected ? 1f : 0f) - anim) * 0.15f;
            anims.put(cat, anim);

            // Фон кнопки
            Color bg = new Color(
                (int)(30 + 25*anim),
                (int)(32 + 28*anim),
                (int)(48 + 52*anim), 255);
            DrawHelper.drawRoundedRect(ctx, x+4, iy, w-8, itemH, 4, bg);

            // Акцент слева если выбрано
            if (anim > 0.05f) {
                DrawHelper.drawRoundedRect(ctx, x+4, iy+(itemH-10)/2f, 2, 10, 1,
                    new Color(120, 130, 255, (int)(255*anim)));
            }

            // Текст
            Color tc = new Color(
                (int)(140 + 115*anim),
                (int)(143 + 112*anim),
                (int)(185 + 70*anim), 255);
            float tx = x + 4 + 8;
            float ty = iy + (itemH - DrawHelper.textHeight()) / 2f;
            DrawHelper.drawText(ctx, cat.getName(), tx, ty, tc);

            bounds.put(cat, new float[]{x+4, iy, w-8, itemH});
        }
    }
}
