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
            if (mx>=b[0]&&mx<=b[0]+b[2]&&my>=b[1]&&my<=b[1]+b[3]) {
                selected = e.getKey(); return true;
            }
        }
        return false;
    }

    public void render(DrawContext ctx, float x, float y, float w, float h) {
        bounds.clear();
        Category[] cats = Category.values();
        float itemH = 20f, pad = 4f;
        float startY = y + 8f;

        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            float iy = startY + i*(itemH+pad);

            float anim = anims.getOrDefault(cat, 0f);
            anim += ((cat==selected?1f:0f)-anim)*0.15f;
            anims.put(cat, anim);

            // Фон кнопки — сильно скруглённый
            Color bg = new Color((int)(27+22*anim),(int)(29+26*anim),(int)(44+52*anim),255);
            DrawHelper.drawRoundedRect(ctx, x+6, iy, w-12, itemH, itemH/2f, bg);

            // Outline при выборе
            if (anim > 0.05f)
                DrawHelper.drawOutline(ctx, x+6, iy, w-12, itemH, itemH/2f,
                    new Color(80,100,230,(int)(120*anim)));

            // Акцент слева
            if (anim > 0.05f)
                DrawHelper.drawRoundedRect(ctx, x+6, iy+(itemH-8)/2f, 2, 8, 1,
                    new Color(110,130,255,(int)(255*anim)));

            // Текст по центру
            Color tc = new Color((int)(120+115*anim),(int)(125+110*anim),(int)(168+75*anim),255);
            float tx = x+6 + (w-12-DrawHelper.tw(cat.getName()))/2f;
            float ty = iy + (itemH - DrawHelper.th())/2f;
            DrawHelper.text(ctx, cat.getName(), tx, ty, tc);

            bounds.put(cat, new float[]{x+6, iy, w-12, itemH});
        }
    }
}
