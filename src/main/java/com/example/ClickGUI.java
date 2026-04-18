package com.example.ui;

import com.example.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ClickGUI extends Screen {

    // Для плавной анимации появления
    private float animationProgress = 0.0f;

    public ClickGUI() {
        super(Text.literal("Nocturn ClickGUI"));
    }

    @Override
    protected void init() {
        super.init();
        // При открытии меню сбрасываем анимацию на 0
        animationProgress = 0.0f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. АНИМАЦИЯ (Плавное увеличение до 1.0)
        animationProgress = MathHelper.lerp(delta * 0.1f, animationProgress, 1.0f);

        // Затемняем фон игры (как в топовых читах)
        context.fillGradient(0, 0, this.width, this.height, 0x90000000, 0x90000000);

        // Применяем масштаб (меню плавно выплывает)
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(animationProgress, animationProgress, 1.0f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        // 2. ОТРИСОВКА ПАНЕЛЕЙ (Категорий)
        int panelWidth = 100;
        int panelHeight = 250;
        int padding = 10;
        
        // Высчитываем центр экрана, чтобы панели были по середине
        int totalWidth = (Category.values().length * panelWidth) + ((Category.values().length - 1) * padding);
        int startX = (this.width - totalWidth) / 2;
        int startY = 40; // Отступ сверху

        for (int i = 0; i < Category.values().length; i++) {
            Category category = Category.values()[i];
            
            int currentX = startX + (i * (panelWidth + padding));

            // Рисуем фон категории (Цвет: Темно-синий/почти черный, полупрозрачный)
            int bgColor = 0xAA0A0A0B; // AA - прозрачность, 0A0A0B - цвет Deep Void
            context.fill(currentX, startY, currentX + panelWidth, startY + panelHeight, bgColor);

            // Рисуем полоску-шапку сверху (Цвет акцента: Electric Azure)
            int accentColor = 0xFF007FFF;
            context.fill(currentX, startY, currentX + panelWidth, startY + 2, accentColor);

            // Рисуем название категории (по центру панели)
            int textWidth = this.textRenderer.getWidth(category.name);
            context.drawTextWithShadow(
                    this.textRenderer, 
                    category.name, 
                    currentX + (panelWidth / 2) - (textWidth / 2), 
                    startY + 8, 
                    0xFFFFFF // Белый текст
            );
        }

        context.getMatrices().pop();
        super.render(context, mouseX, mouseY, delta);
    }

    // Меню не должно ставить игру на паузу (нужно для HvH)
    @Override
    public boolean shouldPause() {
        return false;
    }
}
