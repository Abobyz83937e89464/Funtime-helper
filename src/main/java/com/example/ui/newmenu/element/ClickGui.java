package com.example.ui.newmenu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class ClickGui extends Screen {

    public ClickGui() {
        super(Text.of("Nocturn ClickGUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Рисуем темный фон (чтобы понять, что меню открылось)
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Заголовок в центре
        context.drawCenteredTextWithShadow(this.textRenderer, "Nocturn Client Menu", this.width / 2, 20, 0xFFFFFF);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false; // Чтобы игра не вставала на паузу в одиночке
    }
}
