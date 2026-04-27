package com.example.ui.newmenu.element;

import net.minecraft.client.gui.DrawContext;
import java.awt.Color;

public class CategoryElement {

    // Пустышка для категорий. Позже добавим сюда анимации и шрифты из оригинального кода.
    
    public CategoryElement() {
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        // Логика кликов будет добавлена после переноса компонентов
        return false;
    }

    public void renderFooter(DrawContext context, int x, int y, int footerHeight) {
        // Временная отрисовка футера без кастомных шрифтов и DrawHelper
        context.fill(x, y, x + 800, y + footerHeight, new Color(29, 31, 44, 204).getRGB());
        
        // Временный текст вместо иконок
        context.drawTextWithShadow(context.getMatrices().peek().getPositionMatrix(), 
                net.minecraft.client.MinecraftClient.getInstance().textRenderer, 
                Text.of("Combat | Movement | Render | Utility"), 
                x + 20, y + 20, 
                new Color(197, 200, 255).getRGB());
    }
}
