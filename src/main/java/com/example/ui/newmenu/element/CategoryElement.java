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
            if (mouseX >= b[0] && mouseX <= b[0] + b[2]
             && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
                selectedCategory = entry.getKey();
                return true;
            }
        }
        return false;
    }

    public void renderFooter(DrawContext ctx,
                             float x, float y,
                             float footerW, float footerH,
                             float openAnim) {
        MatrixStack ms = ctx.getMatrices();
        Matrix4f m = ms.peek().getPositionMatrix();
        categoryBounds.clear();

        Category[] cats = Category.values();

        final float BTN_H   = 20f;
        final float BTN_PAD = 10f;
        final float BTN_GAP = 6f;

        // Считаем суммарную ширину
        float totalWidth = 0f;
        for (int i = 0; i < cats.length; i++) {
            totalWidth += Fonts.width(cats[i].getDisplayName()) + BTN_PAD * 2f;
            if (i < cats.length - 1) totalWidth += BTN_GAP;
        }

        float curX = x + (footerW - totalWidth) / 2f;
        float btnY = y + (footerH - BTN_H) / 2f;
        int   a    = (int)(255 * openAnim);

        for (Category cat : cats) {
            // Плавная анимация
            float target = (cat == selectedCategory) ? 1f : 0f;
            float anim   = colorAnims.getOrDefault(cat, 0f);
            anim += (target - anim) * 0.12f;
            colorAnims.put(cat, anim);

            float btnW = Fonts.width(cat.getDisplayName()) + BTN_PAD * 2f;
            float rad  = BTN_H / 2f;

            // Glow под выбранной кнопкой
            if (anim > 0.02f) {
                DrawHelper.drawGlow(m, curX, btnY, btnW, BTN_H, rad, 8,
                    new Color(125, 136, 255,
                        (int)(40 * anim * openAnim)));
            }

            // Фон кнопки
            int bgAlpha = (int)(Math.max(anim, 0.12f) * 180f * openAnim);
            DrawHelper.drawRect(m, curX, btnY, btnW, BTN_H, rad,
                new Color(35, 37, 55, bgAlpha));

            if (anim > 0.02f) {
                // Outline выбранной кнопки
                DrawHelper.drawOutline(ms, curX, btnY, btnW, BTN_H, rad,
                    new Color(100, 110, 230,
                        (int)(130 * anim * openAnim)), 1f);

                // Нижняя линия-акцент
                float lw = (btnW - 8f) * anim;
                float lx = curX + (btnW - lw) / 2f;
                DrawHelper.drawRect(m, lx, btnY + BTN_H - 2f, lw, 2f, 1f,
                    new Color(125, 136, 255,
                        (int)(240 * anim * openAnim)));
                // Glow под линией
                DrawHelper.drawRect(m, lx, btnY + BTN_H - 6f, lw, 6f, 1f,
                    new Color(125, 136, 255,
                        (int)(30 * anim * openAnim)));
            }

            // Цвет текста — lerp
            Color selC = new Color(210, 215, 255);
            Color norC = new Color(100, 103, 150);
            int tr = (int)(norC.getRed()   + (selC.getRed()   - norC.getRed())   * anim);
            int tg = (int)(norC.getGreen() + (selC.getGreen() - norC.getGreen()) * anim);
            int tb = (int)(norC.getBlue()  + (selC.getBlue()  - norC.getBlue())  * anim);

            float tx = curX + (btnW - Fonts.width(cat.getDisplayName())) / 2f;
            float ty = btnY + (footerH - Fonts.height()) / 2f;
            DrawHelper.drawText(ctx, cat.getDisplayName(), tx, ty,
                new Color(tr, tg, tb, a));

            categoryBounds.put(cat, new float[]{ curX, btnY, btnW, BTN_H });
            curX += btnW + BTN_GAP;
        }
    }
}
