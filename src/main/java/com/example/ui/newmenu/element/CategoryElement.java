package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.HashMap;
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

    private static Category selectedCategory = Category.COMBAT;
    private final Map<Category, float[]> categoryBounds = new HashMap<>();
    private final Map<Category, Float> colorAnims = new HashMap<>();
    private final Map<Category, Float> posAnims   = new HashMap<>();

    public CategoryElement() {
        for (Category c : Category.values()) {
            colorAnims.put(c, c == selectedCategory ? 1f : 0f);
            posAnims.put(c, c == selectedCategory ? 1f : 0f);
        }
    }

    public Category getSelectedCategory() { return selectedCategory; }

    public boolean mouseClicked(double mouseX, double mouseY) {
        for (var entry : categoryBounds.entrySet()) {
            float[] b = entry.getValue();
            if (mouseX >= b[0] && mouseX <= b[0]+b[2] && mouseY >= b[1] && mouseY <= b[1]+b[3]) {
                if (selectedCategory != entry.getKey()) {
                    colorAnims.put(selectedCategory, 0f);
                    posAnims.put(selectedCategory, 0f);
                    selectedCategory = entry.getKey();
                    colorAnims.put(selectedCategory, 1f);
                    posAnims.put(selectedCategory, 1f);
                }
                return true;
            }
        }
        return false;
    }

    // Рендер футера — категории горизонтально внизу (как в оригинале)
    public void renderFooter(DrawContext ctx, float x, float y, float footerW, float footerH) {
        MatrixStack ms = ctx.getMatrices();
        categoryBounds.clear();

        Category[] cats = Category.values();

        // Считаем суммарную ширину
        int totalWidth = 0;
        for (int i = 0; i < cats.length; i++) {
            totalWidth += Fonts.width(cats[i].getDisplayName()) + 20;
            if (i < cats.length-1) totalWidth += 10;
        }

        float curX = x + (footerW - totalWidth) / 2f;
        float btnH = 22f;
        float btnY  = y + (footerH - btnH) / 2f;

        for (Category cat : cats) {
            float anim = colorAnims.getOrDefault(cat, 0f);
            anim += ((cat == selectedCategory ? 1f : 0f) - anim) * 0.18f;
            colorAnims.put(cat, anim);

            float btnW = Fonts.width(cat.getDisplayName()) + 20;
            float radius = btnH / 2f; // капсула — как в оригинале

            // Фон кнопки
            Color bg = new Color((int)(29+22*anim),(int)(31+25*anim),(int)(44+50*anim),255);
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), curX, btnY, btnW, btnH, radius, bg);

            // Селектор если выбрано (как в оригинале — тёмная подложка)
            if (anim > 0.02f) {
                DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                    curX, btnY, btnW, btnH, radius,
                    new Color(0, 0, 0, (int)(anim * 60)));
                // Нижняя линия-акцент
                float lw = btnW * 0.7f * anim;
                DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                    curX + (btnW-lw)/2f, btnY+btnH-2, lw, 2, 1,
                    new Color(125, 136, 255, (int)(255*anim)));
                // Outline
                DrawHelper.drawOutline(ms, curX, btnY, btnW, btnH, radius,
                    new Color(80, 90, 200, (int)(100*anim)), 1);
            }

            // Текст
            Color selectedColor = new Color(197, 200, 255);
            Color normalColor   = new Color(141, 144, 199);
            int r2 = (int)(normalColor.getRed()   + (selectedColor.getRed()   - normalColor.getRed())   * anim);
            int g2 = (int)(normalColor.getGreen() + (selectedColor.getGreen() - normalColor.getGreen()) * anim);
            int b2 = (int)(normalColor.getBlue()  + (selectedColor.getBlue()  - normalColor.getBlue())  * anim);

            float tx = curX + (btnW - Fonts.width(cat.getDisplayName())) / 2f;
            float ty = btnY + (btnH - Fonts.height()) / 2f;
            DrawHelper.drawText(ctx, cat.getDisplayName(), tx, ty, new Color(r2,g2,b2));

            categoryBounds.put(cat, new float[]{curX, btnY, btnW, btnH});
            curX += btnW + 10;
        }
    }
}
