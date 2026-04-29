package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryElement {

    public enum Category {
        COMBAT("Combat", "⚔"),
        MOVEMENT("Movement", "🏃"),
        RENDER("Render", "👁"),
        PLAYER("Player", "🧑"),
        WORLD("World", "🌍"),
        MISC("Misc", "⚙");

        private final String displayName;
        private final String icon;

        Category(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIcon() {
            return icon;
        }
    }

    private Category selectedCategory = Category.COMBAT;
    private final Map<Category, int[]> categoryBounds = new LinkedHashMap<>();

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Category category) {
        this.selectedCategory = category;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        for (Map.Entry<Category, int[]> entry : categoryBounds.entrySet()) {
            int[] bounds = entry.getValue();
            if (mouseX >= bounds[0] && mouseX <= bounds[0] + bounds[2] &&
                mouseY >= bounds[1] && mouseY <= bounds[1] + bounds[3]) {
                selectedCategory = entry.getKey();
                return true;
            }
        }
        return false;
    }

    public void render(DrawContext context, int footerX, int footerY, int footerWidth, int footerHeight) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Category[] categories = Category.values();

        int spacing = 24;
        int totalWidth = 0;
        for (Category cat : categories) {
            totalWidth += textRenderer.getWidth(cat.getDisplayName()) + 4;
        }
        totalWidth += spacing * (categories.length - 1);

        int currentX = footerX + (footerWidth - totalWidth) / 2;
        int centerY = footerY + (footerHeight - 20) / 2;

        categoryBounds.clear();

        for (Category category : categories) {
            String name = category.getDisplayName();
            int nameWidth = textRenderer.getWidth(name) + 4;
            boolean isSelected = (category == selectedCategory);

            int btnWidth = nameWidth + 16;
            int btnHeight = 20;
            int btnX = currentX - 8;
            int btnY = centerY;

            if (isSelected) {
                // Выбранная категория — подсветка
                DrawHelper.drawRect(context, btnX, btnY, btnWidth, btnHeight,
                        new Color(51, 56, 94));
                DrawHelper.drawOutlineRect(context, btnX, btnY, btnWidth, btnHeight,
                        new Color(75, 80, 140), 1);
            } else {
                // Ховер эффект — чуть светлее при наведении
                DrawHelper.drawRect(context, btnX, btnY, btnWidth, btnHeight,
                        new Color(35, 37, 50));
            }

            Color textColor = isSelected ? new Color(197, 200, 255) : new Color(110, 114, 160);
            context.drawText(textRenderer, name, currentX, centerY + 6, textColor.getRGB(), false);

            categoryBounds.put(category, new int[]{btnX, btnY, btnWidth, btnHeight});
            currentX += nameWidth + spacing;
        }
    }
}
