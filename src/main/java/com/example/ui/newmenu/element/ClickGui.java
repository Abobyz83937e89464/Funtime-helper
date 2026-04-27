package com.example.ui.newmenu;

import com.example.util.render.DrawHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.awt.Color;

public class ClickGui extends Screen {

    // Размеры и позиция главного окна
    private int x = 100;
    private int y = 80;
    private int width = 450;
    private int height = 300;

    public ClickGui() {
        super(Text.of("Nocturn Client"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Затемнение заднего фона игры
        this.renderBackground(context, mouseX, mouseY, delta);

        // 1. Основной фон меню (Темно-серый, почти черный)
        int bgColor = new Color(15, 15, 15, 245).getRGB();
        DrawHelper.drawRect(context, x, y, width, height, bgColor);

        // 2. Верхняя плашка (Акцентный цвет, стиль Minced/Nocturn)
        int accentColor = new Color(138, 43, 226).getRGB(); // Фиолетовый
        DrawHelper.drawRect(context, x, y, width, 25, accentColor);
        
        // Название клиента в шапке
        context.drawTextWithShadow(this.textRenderer, "NOCTURN CLIENT", x + 10, y + 8, -1);

        // 3. Панель категорий (Левая колонка)
        int catPanelWidth = 100;
        DrawHelper.drawRect(context, x, y + 25, catPanelWidth, height - 25, new Color(22, 22, 22, 255).getRGB());

        // Рисуем сами категории
        String[] categories = {"Combat", "Movement", "Render", "Player", "Misc"};
        int catY = y + 40;
        for (String cat : categories) {
            // Если мышка наведена на категорию - делаем текст белым, иначе серым
            boolean isHovered = mouseX >= x && mouseX <= x + catPanelWidth && mouseY >= catY - 5 && mouseY <= catY + 15;
            int textColor = isHovered ? -1 : new Color(170, 170, 170).getRGB();
            
            context.drawTextWithShadow(this.textRenderer, cat, x + 15, catY, textColor);
            catY += 30; // Отступ между категориями
        }

        // 4. Панель модулей (Правая часть)
        int modX = x + catPanelWidth + 15;
        int modY = y + 40;
        
        // Фейковые модули для визуала (потом привяжем к реальным)
        String[] modules = {"Aura", "AutoTotem", "Velocity", "TargetStrafe", "NoFall"};
        for (String mod : modules) {
            // Фон плашки модуля
            DrawHelper.drawRect(context, modX, modY, 315, 35, new Color(30, 30, 30, 255).getRGB());
            
            // Название модуля
            context.drawTextWithShadow(this.textRenderer, mod, modX + 10, modY + 14, -1);
            
            // Имитация кнопки включения (чекбокс)
            DrawHelper.drawRect(context, modX + 280, modY + 10, 15, 15, new Color(45, 45, 45).getRGB());
            
            modY += 45; // Отступ между модулями
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false; // Обязательно false, чтобы на анархиях не кикало
    }
}
