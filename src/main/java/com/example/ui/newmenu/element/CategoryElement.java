package com.example.ui.newmenu.element;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Category selectedCategory = Category.COMBAT;
    private final Map<Category, float[]> categoryBounds = new LinkedHashMap<>();

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        for (Map.Entry<Category, float[]> entry : categoryBounds.entrySet()) {
            float[] bounds = entry.getValue();
            if (mouseX >= bounds[0] && mouseX <= bounds[0] + bounds[2] &&
                mouseY >= bounds[1] && mouseY <= bounds[1] + bounds[3]) {
                selectedCategory = entry.getKey();
                return true;
            }
        }
        return false;
    }

    public void renderFooter(DrawContext context, int x, int y, int footerHeight) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Category[] categories = Category.values();

        int totalWidth = 0;
        int spacing = 40;
        for (Category cat : categories) {
            totalWidth += textRenderer.getWidth(cat.getDisplayName());
        }
        totalWidth += spacing * (categories.length - 1);

        float currentX = x + (1100 - totalWidth) / 2f;
        float textY = y + (footerHeight - 8) / 2f;

        categoryBounds.clear();

        for (Category category : categories) {
            String name = category.getDisplayName();
            int nameWidth = textRenderer.getWidth(name);

            boolean isSelected = (category == selectedCategory);
            Color color = isSelected ? new Color(197, 200, 255) : new Color(141, 144, 199);

            // Подсветка выбранной категории
            if (isSelected) {
                com.example.util.render.DrawHelper.drawRect(context,
                        currentX - 6, textY - 4,
                        nameWidth + 12, 16, 5,
                        new Color(0, 0, 0, 180));
            }

            context.drawText(textRenderer, name, (int) currentX, (int) textY, color.getRGB(), false);

            categoryBounds.put(category, new float[]{currentX - 6, textY - 4, nameWidth + 12, 16});

            currentX += nameWidth + spacing;
        }
    }
}
