package com.example.ui.menu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryElement {

    // ─── Категории ───────────────────────────────────────────────────────────

    public enum Category {
        COMBAT  ("Combat",   "⚔"),
        MOVEMENT("Movement", "➤"),
        RENDER  ("Render",   "◈"),
        PLAYER  ("Player",   "♟"),
        WORLD   ("World",    "◉"),
        MISC    ("Misc",     "⚙");

        private final String displayName;
        private final String icon;

        Category(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon()        { return icon; }
    }

    // ─── Состояние ───────────────────────────────────────────────────────────

    private Category selectedCategory = Category.COMBAT;
    private final Map<Category, int[]> categoryBounds = new LinkedHashMap<>();

    // Плавная анимация выделения (простой lerp)
    private final Map<Category, Float> selectionAnim = new LinkedHashMap<>();

    public CategoryElement() {
        for (Category c : Category.values()) {
            selectionAnim.put(c, c == selectedCategory ? 1f : 0f);
        }
    }

    // ─── API ─────────────────────────────────────────────────────────────────

    public Category getSelectedCategory() { return selectedCategory; }

    public boolean mouseClicked(double mouseX, double mouseY) {
        for (Map.Entry<Category, int[]> entry : categoryBounds.entrySet()) {
            int[] b = entry.getValue();
            if (mouseX >= b[0] && mouseX <= b[0] + b[2] &&
                mouseY >= b[1] && mouseY <= b[1] + b[3]) {
                selectedCategory = entry.getKey();
                return true;
            }
        }
        return false;
    }

    // ─── Рендер футера ───────────────────────────────────────────────────────

    public void render(DrawContext context, int footerX, int footerY, int footerWidth, int footerHeight) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        Category[] cats = Category.values();

        // === Цвета ===
        Color selectedText  = new Color(197, 200, 255);
        Color normalText    = new Color(100, 104, 150);
        Color selectedBg    = new Color(38, 40, 62);
        Color selectedLine  = new Color(125, 136, 255);

        // Считаем суммарную ширину для центрирования
        int spacing = 18;
        int totalWidth = 0;
        for (Category c : cats) {
            totalWidth += tr.getWidth(c.getDisplayName()) + 20; // padding по 10 с каждой стороны
        }
        totalWidth += spacing * (cats.length - 1);

        int startX = footerX + (footerWidth - totalWidth) / 2;
        int btnH = footerHeight - 8;
        int btnY = footerY + 4;

        categoryBounds.clear();

        int cx = startX;
        for (Category cat : cats) {
            // Анимация lerp
            float target = (cat == selectedCategory) ? 1f : 0f;
            float cur = selectionAnim.getOrDefault(cat, 0f);
            float next = cur + (target - cur) * 0.2f;
            selectionAnim.put(cat, next);

            int nameW = tr.getWidth(cat.getDisplayName());
            int btnW = nameW + 20;

            // Фон кнопки
            if (next > 0.01f) {
                int bgAlpha = (int) (next * 255);
                DrawHelper.drawRoundedRect(context, cx, btnY, btnW, btnH, 4,
                        new Color(selectedBg.getRed(), selectedBg.getGreen(), selectedBg.getBlue(), bgAlpha));
            }

            // Текст
            int r = blend(normalText.getRed(),   selectedText.getRed(),   next);
            int g = blend(normalText.getGreen(), selectedText.getGreen(), next);
            int b = blend(normalText.getBlue(),  selectedText.getBlue(),  next);
            context.drawText(tr, cat.getDisplayName(), cx + 10, btnY + (btnH - 8) / 2, new Color(r, g, b).getRGB(), false);

            // Нижняя линия-акцент для выбранного
            if (next > 0.01f) {
                int lineAlpha = (int) (next * 255);
                DrawHelper.drawAccentBar(context,
                        cx, btnY + btnH - 2,
                        btnW, 2);
            }

            categoryBounds.put(cat, new int[]{cx, btnY, btnW, btnH});
            cx += btnW + spacing;
        }
    }

    private int blend(int a, int b, float t) {
        return (int) (a + (b - a) * t);
    }
}
