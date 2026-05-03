package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CategoryElement {

    // ── Палитра ───────────────────────────────────────────────
    private static final Color ACCENT      = new Color(0x6C, 0x7B, 0xFF);
    private static final Color TEXT_SEL    = new Color(0xE6, 0xE9, 0xFF);
    private static final Color TEXT_NOR    = new Color(0x8A, 0x90, 0xC2);
    private static final Color BTN_SEL_BG  = new Color(0x22, 0x25, 0x38);
    private static final Color BTN_NOR_BG  = new Color(0x16, 0x17, 0x21);

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
    private final Map<Category, float[]> bounds     = new HashMap<>();
    private final Map<Category, Float>   anims      = new HashMap<>();

    public CategoryElement() {
        for (Category c : Category.values())
            anims.put(c, c == selectedCategory ? 1f : 0f);
    }

    public Category getSelectedCategory() { return selectedCategory; }

    public boolean mouseClicked(double mx, double my) {
        for (var e : bounds.entrySet()) {
            float[] b = e.getValue();
            if (mx >= b[0] && mx <= b[0] + b[2] && my >= b[1] && my <= b[1] + b[3]) {
                if (selectedCategory != e.getKey()) {
                    anims.put(selectedCategory, 0f);
                    selectedCategory = e.getKey();
                    anims.put(selectedCategory, 1f);
                }
                return true;
            }
        }
        return false;
    }

    public void renderFooter(DrawContext ctx, float x, float y, float footerW, float footerH) {
        MatrixStack ms = ctx.getMatrices();
        bounds.clear();

        Category[] cats = Category.values();

        // Суммарная ширина
        int totalWidth = 0;
        for (int i = 0; i < cats.length; i++) {
            totalWidth += Fonts.mediumWidth(cats[i].getDisplayName()) + 18;
            if (i < cats.length - 1) totalWidth += 8;
        }

        float curX = x + (footerW - totalWidth) / 2f;
        float btnH = 20f;
        float btnY = y + (footerH - btnH) / 2f;

        for (Category cat : cats) {
            float target = cat == selectedCategory ? 1f : 0f;
            float anim   = anims.getOrDefault(cat, 0f);
            anim += (target - anim) * 0.14f;
            anims.put(cat, anim);

            float btnW  = Fonts.mediumWidth(cat.getDisplayName()) + 18;
            float radius = btnH / 2f;

            // Тень под выбранной кнопкой
            if (anim > 0.05f)
                DrawHelper.drawShadow(ms, curX, btnY, btnW, btnH, radius, 6f,
                    new Color(108, 123, 255, (int)(80 * anim)));

            // Фон кнопки
            Color bg = lerpColor(BTN_NOR_BG, BTN_SEL_BG, anim);
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), curX, btnY, btnW, btnH, radius, bg);

            // Outline выбранной
            if (anim > 0.02f)
                DrawHelper.drawOutline(ms, curX - 1, btnY - 1, btnW + 2, btnH + 2, radius,
                    new Color(108, 123, 255, (int)(110 * anim)), 1);

            // Нижняя линия-акцент
            if (anim > 0.02f) {
                float lw = (btnW - 8) * anim;
                DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                    curX + (btnW - lw) / 2f, btnY + btnH - 2, lw, 2, 1,
                    new Color(108, 123, 255, (int)(240 * anim)));
                // Glow
                DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                    curX + (btnW - lw) / 2f, btnY + btnH - 4, lw, 4, 1,
                    new Color(108, 123, 255, (int)(40 * anim)));
            }

            // Текст
            Color textC = lerpColor(TEXT_NOR, TEXT_SEL, anim);
            float tx = curX + (btnW - Fonts.mediumWidth(cat.getDisplayName())) / 2f;
            float ty = btnY + (btnH - Fonts.height()) / 2f;
            DrawHelper.drawTextMedium(ctx, cat.getDisplayName(), tx, ty, textC);

            bounds.put(cat, new float[]{curX, btnY, btnW, btnH});
            curX += btnW + 8;
        }
    }

    private Color lerpColor(Color a, Color b, float t) {
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }
}
