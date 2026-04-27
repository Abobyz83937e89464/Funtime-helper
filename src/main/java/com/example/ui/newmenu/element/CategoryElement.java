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

    public void renderFooter(DrawContext context, int x, int y, int footerWidth, int footerHeight) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Category[] categories = Category.values();

        int spacing = 8;
        int totalWidth = 0;
        for (Category cat : categories) {
            totalWidth += textRenderer.getWidth(cat.getDisplayName());
        }
        totalWidth += spacing * (categories.length - 1);

        float currentX = x + (footerWidth - totalWidth) / 2f;
        float textY = y + (footerHeight - 8) / 2f;

        categoryBounds.clear();

        for (Category category : categories) {
            String name = category.getDisplayName();
            int nameWidth = textRenderer.getWidth(name);

            boolean isSelected = (category == selectedCategory);

            if (isSelected) {
                DrawHelper.drawRect(context,
                        currentX - 3, textY - 2,
                        nameWidth + 6, 12, 3,
                        new Color(0, 0, 0, 180));
            }

            Color color = isSelected ? new Color(197, 200, 255) : new Color(141, 144, 199);
            context.drawText(textRenderer, name, (int) currentX, (int) textY, color.getRGB(), false);

            categoryBounds.put(category, new float[]{currentX - 3, textY - 2, nameWidth + 6, 12});
            currentX += nameWidth + spacing;
        }
    }
}
