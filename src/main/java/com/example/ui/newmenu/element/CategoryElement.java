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
        MOVEMENT("Move"),
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
    private final Map<Category, Float>   colorAnims     = new HashMap<>();

    public CategoryElement() {
        for (Category c : Category.values())
            colorAnims.put(c, c == selectedCategory ? 1f : 0f);
    }

    public Category getSelectedCategory() { return selectedCategory; }

    public boolean mouseClicked(double mouseX, double mouseY) {
        for (var entry : categoryBounds.entrySet()) {
            float[] b = entry.getValue();
            if (mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
                if (selectedCategory != entry.getKey()) {
                    colorAnims.put(selectedCategory, 0f);
                    selectedCategory = entry.getKey();
                    colorAnims.put(selectedCategory, 1f);
                }
                return true;
            }
        }
        return false;
    }

    public void renderFooter(DrawContext ctx, float x, float y, float footerW, float footerH) {
        MatrixStack ms = ctx.getMatrices();
        categoryBounds.clear();

        Category[] cats = Category.values();

        // Суммарная ширина кнопок
        int totalWidth = 0;
        for (int i = 0; i < cats.length; i++) {
            totalWidth += Fonts.mediumWidth(cats[i].getDisplayName()) + 16;
            if (i < cats.length - 1) totalWidth += 6;
        }

        float curX = x + (footerW - totalWidth) / 2f;
        float btnH = 18f;
        float btnY = y + (footerH - btnH) / 2f;

        for (Category cat : cats) {
            float target = cat == selectedCategory ? 1f : 0f;
            float anim   = colorAnims.getOrDefault(cat, 0f);
            anim += (target - anim) * 0.14f;
            colorAnims.put(cat, anim);

            float btnW  = Fonts.mediumWidth(cat.getDisplayName()) + 16;
            float radius = btnH / 2f;

            // Фон кнопки
            int bgR = (int)(22 + 18 * anim);
            int bgG = (int)(24 + 20 * anim);
            int bgB = (int)(35 + 42 * anim);
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), curX, btnY, btnW, btnH, radius,
                new Color(bgR, bgG, bgB));

            if (anim > 0.02f) {
                // Тёмный оверлей
                DrawHelper.drawRect(ms.peek().getPositionMatrix(), curX, btnY, btnW, btnH, radius,
                    new Color(0, 0, 0, (int)(40 * anim)));
                // Нижняя линия-акцент
                float lw = (btnW - 8) * anim;
                DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                    curX + (btnW - lw) / 2f, btnY + btnH - 2, lw, 2, 1,
                    new Color(110, 120, 255, (int)(240 * anim)));
                // Outline
                DrawHelper.drawOutline(ms, curX - 1, btnY - 1, btnW + 2, btnH + 2, radius,
                    new Color(70, 80, 200, (int)(100 * anim)), 1);
            }

            // Текст Inter Medium
            Color selC = new Color(197, 200, 255);
            Color norC = new Color(110, 115, 160);
            int tr = (int)(norC.getRed()   + (selC.getRed()   - norC.getRed())   * anim);
            int tg = (int)(norC.getGreen() + (selC.getGreen() - norC.getGreen()) * anim);
            int tb = (int)(norC.getBlue()  + (selC.getBlue()  - norC.getBlue())  * anim);

            float tx2 = curX + (btnW - Fonts.mediumWidth(cat.getDisplayName())) / 2f;
            float ty2 = btnY + (btnH - Fonts.height()) / 2f;
            DrawHelper.drawTextMedium(ctx, cat.getDisplayName(), tx2, ty2, new Color(tr, tg, tb));

            categoryBounds.put(cat, new float[]{curX, btnY, btnW, btnH});
            curX += btnW + 6;
        }
    }
}
