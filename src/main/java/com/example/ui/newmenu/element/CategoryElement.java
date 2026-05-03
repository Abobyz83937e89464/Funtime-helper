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

        // Считаем суммарную ширину
        int totalWidth = 0;
        for (int i = 0; i < cats.length; i++) {
            totalWidth += Fonts.mediumWidth(cats[i].getDisplayName()) + 20;
            if (i < cats.length - 1) totalWidth += 8;
        }

        float curX = x + (footerW - totalWidth) / 2f;
        float btnH = 20f;
        float btnY = y + (footerH - btnH) / 2f;

        for (Category cat : cats) {
            // плавная анимация
            float target = cat == selectedCategory ? 1f : 0f;
            float anim   = colorAnims.getOrDefault(cat, 0f);
            anim += (target - anim) * 0.14f;
            colorAnims.put(cat, anim);

            float btnW  = Fonts.mediumWidth(cat.getDisplayName()) + 20;
            float radius = btnH / 2f;

            // Фон кнопки
            int bgR = (int)(29 + 22 * anim);
            int bgG = (int)(31 + 25 * anim);
            int bgB = (int)(44 + 50 * anim);
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), curX, btnY, btnW, btnH, radius, new Color(bgR, bgG, bgB));

            // Акцент при выборе
            if (anim > 0.02f) {
                // тёмный оверлей
                DrawHelper.drawRect(ms.peek().getPositionMatrix(), curX, btnY, btnW, btnH, radius,
                    new Color(0, 0, 0, (int)(50 * anim)));
                // нижняя линия
                float lw = (btnW - 10) * anim;
                DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                    curX + (btnW - lw) / 2f, btnY + btnH - 2, lw, 2, 1,
                    new Color(125, 136, 255, (int)(255 * anim)));
                // outline
                DrawHelper.drawOutline(ms, curX, btnY, btnW, btnH, radius,
                    new Color(80, 90, 200, (int)(120 * anim)), 1);
            }

            // Текст Inter Medium
            Color selC = new Color(197, 200, 255);
            Color norC = new Color(141, 144, 199);
            int tr = (int)(norC.getRed()   + (selC.getRed()   - norC.getRed())   * anim);
            int tg = (int)(norC.getGreen() + (selC.getGreen() - norC.getGreen()) * anim);
            int tb = (int)(norC.getBlue()  + (selC.getBlue()  - norC.getBlue())  * anim);

            float tx = curX + (btnW - Fonts.mediumWidth(cat.getDisplayName())) / 2f;
            float ty = btnY + (btnH - Fonts.height()) / 2f;
            DrawHelper.drawTextMedium(ctx, cat.getDisplayName(), tx, ty, new Color(tr, tg, tb));

            categoryBounds.put(cat, new float[]{curX, btnY, btnW, btnH});
            curX += btnW + 8;
        }
    }
}
