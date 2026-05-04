package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

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

    // Bounds каждой кнопки категории: [x, y, w, h]
    private final Map<Category, float[]> categoryBounds = new HashMap<>();

    // Анимации: значения от 0.0 до 1.0
    private final Map<Category, Float> colorAnims = new HashMap<>();

    public CategoryElement() {
        for (Category c : Category.values()) {
            colorAnims.put(c, c == selectedCategory ? 1f : 0f);
        }
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        for (var entry : categoryBounds.entrySet()) {
            float[] b = entry.getValue();
            if (mouseX >= b[0] && mouseX <= b[0] + b[2]
             && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
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

    /**
     * Рисует кнопки категорий внутри футера.
     * openAnim — значение 0..1 для плавного появления меню.
     */
    public void renderFooter(DrawContext ctx, float x, float y,
                             float footerW, float footerH, float openAnim) {
        MatrixStack ms = ctx.getMatrices();
        Matrix4f m = ms.peek().getPositionMatrix();
        categoryBounds.clear();

        Category[] cats = Category.values();

        // Считаем суммарную ширину всех кнопок
        float btnH     = 20f;
        float btnPad   = 8f;  // горизонтальный отступ текста внутри кнопки
        float btnGap   = 6f;  // отступ между кнопками

        float totalWidth = 0f;
        for (int i = 0; i < cats.length; i++) {
            totalWidth += Fonts.width(cats[i].getDisplayName()) + btnPad * 2;
            if (i < cats.length - 1) totalWidth += btnGap;
        }

        float curX = x + (footerW - totalWidth) / 2f;
        float btnY = y + (footerH - btnH) / 2f;
        int   a    = (int) (255 * openAnim);

        for (Category cat : cats) {
            // Плавная анимация
            float target = (cat == selectedCategory) ? 1f : 0f;
            float anim   = colorAnims.getOrDefault(cat, 0f);
            anim += (target - anim) * 0.20f;
            colorAnims.put(cat, anim);

            float btnW  = Fonts.width(cat.getDisplayName()) + btnPad * 2;
            float rad   = btnH / 2f;  // капсула

            // Фон кнопки — темнее для невыбранных, ярче для выбранных
            if (anim > 0.02f) {
                // Подложка (тёмная) — видна при выбранном состоянии
                DrawHelper.drawRect(m, curX, btnY, btnW, btnH, rad,
                    new Color(0, 0, 0, (int) (55 * anim * (a / 255f))));

                // Фиолетовый glow при выбранном состоянии
                DrawHelper.drawGlow(m, curX, btnY, btnW, btnH, rad, 6,
                    new Color(125, 136, 255, (int) (35 * anim * (a / 255f))));

                // Нижняя синяя линия-акцент
                float lw = (btnW - 8f) * anim;
                DrawHelper.drawRect(m,
                    curX + (btnW - lw) / 2f, btnY + btnH - 2f, lw, 2f, 1f,
                    new Color(125, 136, 255, (int) (255 * anim * (a / 255f))));

                // Outline
                DrawHelper.drawOutline(ms, curX, btnY, btnW, btnH, rad,
                    new Color(80, 90, 200, (int) (110 * anim * (a / 255f))), 1);
            }

            // Цвет текста: интерполяция normal → selected
            Color selColor = new Color(197, 200, 255);
            Color nrmColor = new Color(100, 103, 145);
            int r2 = (int)(nrmColor.getRed()   + (selColor.getRed()   - nrmColor.getRed())   * anim);
            int g2 = (int)(nrmColor.getGreen() + (selColor.getGreen() - nrmColor.getGreen()) * anim);
            int b2 = (int)(nrmColor.getBlue()  + (selColor.getBlue()  - nrmColor.getBlue())  * anim);

            float tx = curX + (btnW - Fonts.width(cat.getDisplayName())) / 2f;
            float ty = btnY + (btnH - Fonts.height()) / 2f;
            DrawHelper.drawText(ctx, cat.getDisplayName(), tx, ty, new Color(r2, g2, b2, a));

            categoryBounds.put(cat, new float[]{ curX, btnY, btnW, btnH });
            curX += btnW + btnGap;
        }
    }
}
