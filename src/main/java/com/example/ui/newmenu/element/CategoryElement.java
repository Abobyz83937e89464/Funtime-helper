package com.example.ui.newmenu.element;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text; // ВОТ ЭТОТ ИМПОРТ ВСЁ ПОЧИНИТ
import java.awt.Color;

public class CategoryElement {

    public CategoryElement() {
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        return false;
    }

    public void renderFooter(DrawContext context, int x, int y, int footerHeight) {
        // Отрисовка подложки
        context.fill(x, y, x + 800, y + footerHeight, new Color(29, 31, 44, 204).getRGB());
        
        // Отрисовка текста (теперь Text.of заработает)
        context.drawTextWithShadow(
                net.minecraft.client.MinecraftClient.getInstance().textRenderer, 
                Text.of("Combat | Movement | Render | Utility"), 
                x + 20, y + 20, 
                new Color(197, 200, 255).getRGB());
    }
}
